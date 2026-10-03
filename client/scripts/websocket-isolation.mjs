/**
 * Run from client: node scripts/websocket-isolation.mjs
 * Requires Node 22 and the existing @stomp/stompjs package.
 * Creates two local test messes; registers, approves, and logs in customers.
 * Approval triggers an email attempt. SMTP delivery is not tested.
 * Tests the raw WebSocket transport, not browser SockJS fallbacks.
 */
import assert from "node:assert/strict";
import { randomInt, randomUUID } from "node:crypto";
import { Client } from "@stomp/stompjs";

const base = new URL(
  process.env.SMART_MESS_BASE_URL || "http://localhost:8080"
);

if (
  base.protocol !== "http:" ||
  !["localhost", "127.0.0.1", "[::1]"].includes(base.hostname) ||
  base.pathname !== "/" ||
  base.username ||
  base.password ||
  base.search ||
  base.hash
) {
  throw new Error("Only a local HTTP backend origin is allowed.");
}

const wsUrl = new URL("/ws-dashboard/websocket", base);
wsUrl.protocol = "ws:";

const pause = (ms) =>
  new Promise((resolve) => setTimeout(resolve, ms));

const sockets = [];
const tenants = [];
const run = `${Date.now()}-${randomUUID().slice(0, 8)}`;
const password = "Password@123";

let passed = 0;
let failed = 0;

async function api(
  method,
  path,
  token,
  body,
  status = 200,
  expectedSuccess = true
) {
  const response = await fetch(new URL(path, base), {
    method,
    signal: AbortSignal.timeout(30000),
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    ...(body === undefined
      ? {}
      : { body: JSON.stringify(body) }),
  });

  const result = await response.json();

  assert.equal(
    response.status,
    status,
    `${method} ${path}: HTTP ${response.status}; ${
      result.message || ""
    }`
  );

  assert.equal(
    result.success,
    expectedSuccess,
    `${method} ${path}: ${
      result.message || "Unsuccessful response"
    }`
  );

  return result.data;
}

async function check(name, action) {
  try {
    await action();
    passed++;
    console.log(`PASS  ${name}`);
  } catch (error) {
    failed++;
    console.error(`FAIL  ${name}: ${error.message}`);
  }
}

function socket(token) {
  const session = {
    connected: false,
    closed: false,
    errors: [],
    messages: [],
    parseErrors: [],
  };

  session.client = new Client({
    webSocketFactory: () =>
      new WebSocket(wsUrl.href, ["v12.stomp"]),

    connectHeaders: token
      ? { Authorization: `Bearer ${token}` }
      : {},

    reconnectDelay: 0,
    connectionTimeout: 8000,
    heartbeatIncoming: 0,
    heartbeatOutgoing: 0,
    debug: () => {},

    onConnect: () => {
      session.connected = true;
    },

    onStompError: (frame) => {
      session.errors.push(
        frame.headers.message ||
          frame.body ||
          "STOMP ERROR"
      );
    },

    onWebSocketClose: () => {
      session.closed = true;
    },

    onWebSocketError: () => {},
  });

  sockets.push(session);
  session.client.activate();

  return session;
}

async function until(
  predicate,
  reason,
  timeout = 8000
) {
  const deadline = Date.now() + timeout;

  while (Date.now() < deadline) {
    if (predicate()) return;
    await pause(50);
  }

  throw new Error(reason);
}

async function connect(token) {
  const session = socket(token);

  await until(
    () =>
      session.connected ||
      session.errors.length ||
      session.closed,
    "No STOMP CONNECTED frame"
  );

  assert.equal(
    session.connected,
    true,
    "Valid account did not receive STOMP CONNECTED"
  );

  healthy(session);
  return session;
}

function healthy(session) {
  assert.equal(
    session.client.connected,
    true,
    "Positive-control socket disconnected"
  );

  assert.equal(
    session.closed,
    false,
    "Positive-control socket closed"
  );

  assert.equal(
    session.errors.length,
    0,
    "Unexpected STOMP ERROR on positive-control socket"
  );

  assert.equal(
    session.parseErrors.length,
    0,
    "Received a message that was not JSON"
  );
}

function subscribe(session, destination) {
  session.client.subscribe(destination, (message) => {
    try {
      session.messages.push(JSON.parse(message.body));
    } catch {
      session.parseErrors.push("Message JSON parse failed");
    }
  });
}

async function stop(session) {
  await session.client.deactivate({ force: true });
}

async function rejected(token, action) {
  const session = action
    ? await connect(token)
    : socket(token);

  try {
    if (action) action(session.client);

    await until(
      () => session.errors.length > 0,
      "No explicit STOMP ERROR received. A timeout/connection close is not proof of rejection."
    );

    if (!action) {
      assert.equal(
        session.connected,
        false,
        "Invalid authentication received CONNECTED"
      );
    }
  } finally {
    await stop(session);
  }
}

const mobileSuffix = String(
  randomInt(100000000, 999999999)
);

const parts = new Intl.DateTimeFormat("en-GB", {
  timeZone: "Asia/Kolkata",
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
}).formatToParts(new Date());

const datePart = (type) =>
  parts.find((part) => part.type === type).value;

const today =
  `${datePart("year")}-${datePart("month")}-${datePart("day")}`;

async function setup(label) {
  const owner = await api(
    "POST",
    "/api/auth/owner/register",
    null,
    {
      fullName: `WebSocket Owner ${label}`,
      messName: `WebSocket Test ${label} ${run}`,
      mobileNumber:
        `${label === "A" ? "9" : "8"}${mobileSuffix}`,
      email:
        `ws.owner.${label.toLowerCase()}.${run}@example.com`,
      password,
    },
    201
  );

  const tenant = {
    label,
    ownerToken: owner.accessToken,
    messId: owner.messId,
    windowChanged: false,
  };

  tenants.push(tenant);

  assert.ok(
    tenant.ownerToken && tenant.messId,
    "Owner login response needs accessToken and messId"
  );

  const link = await api(
    "GET",
    "/api/mess/registration-link",
    tenant.ownerToken
  );

  const customer = await api(
    "POST",
    "/api/auth/customer/register",
    null,
    {
      fullName: `WebSocket Customer ${label}`,
      mobileNumber:
        `${label === "A" ? "7" : "6"}${mobileSuffix}`,
      email:
        `ws.customer.${label.toLowerCase()}.${run}@example.com`,
      password,
      registrationCode: link.registrationCode,
    },
    201
  );

  assert.equal(
    customer.status,
    "PENDING",
    "Registration must await approval"
  );

  assert.equal(
    Object.hasOwn(customer, "accessToken"),
    false,
    "Pending registration must not issue an access token"
  );

  tenant.customerId = customer.customerId;
  tenant.customerEmail =
    `ws.customer.${label.toLowerCase()}.${run}@example.com`;

  assert.ok(
    tenant.customerId,
    "Registration response missing customer ID"
  );

  await api(
    "POST",
    "/api/auth/customer/login",
    null,
    { email: tenant.customerEmail, password },
    400,
    false
  );

  const approved = await api(
    "PATCH",
    `/api/customers/${tenant.customerId}/approve`,
    tenant.ownerToken
  );

  assert.equal(approved.customerId, tenant.customerId);
  assert.equal(approved.status, "ACTIVE");

  const login = await api(
    "POST",
    "/api/auth/customer/login",
    null,
    { email: tenant.customerEmail, password }
  );

  assert.equal(login.customerId, tenant.customerId);
  tenant.customerToken = login.accessToken;

  assert.ok(
    tenant.customerToken,
    "Approved customer login missing token"
  );

  await api(
    "PUT",
    "/api/settings/response-window",
    tenant.ownerToken,
    {
      lunchResponseCutoff: "23:59:59",
      dinnerResponseCutoff: "23:59:59",
    }
  );

  tenant.windowChanged = true;

  const menu = await api(
    "POST",
    "/api/menus",
    tenant.ownerToken,
    {
      menuDate: today,
      mealSession: "DINNER",
      sabjiOne: "WebSocket Test Paneer",
      sabjiTwo: "Aloo Gobi",
      dal: "Dal Tadka",
      rice: "Jeera Rice",
      sweet: "Kheer",
    },
    201
  );

  tenant.menuId = menu.menuId;
  return tenant;
}

async function dashboardChange(tenant, own, other) {
  healthy(own);
  healthy(other);

  const ownBefore = own.messages.length;
  const otherBefore = other.messages.length;

  await api(
    "POST",
    "/api/meal-responses",
    tenant.customerToken,
    {
      menuId: tenant.menuId,
      responseStatus: "ACCEPTED",
      mealOption: "FULL",
      extraRotiCount: 1,
    }
  );

  await until(
    () =>
      own.messages.slice(ownBefore).some(
        (message) =>
          message.menuId === tenant.menuId &&
          message.acceptedResponses === 1
      ),
    "Own dashboard did not receive the updated meal response"
  );

  await pause(1500);

  healthy(own);
  healthy(other);

  const received = own.messages.slice(ownBefore);

  assert.ok(
    received.every(
      (message) => message.menuId === tenant.menuId
    ),
    "Foreign dashboard data received"
  );

  assert.equal(
    other.messages.length,
    otherBefore,
    "Other mess received this dashboard update"
  );
}

async function pricingChange(
  tenant,
  own,
  other,
  pricing
) {
  healthy(own);
  healthy(other);

  const ownBefore = own.messages.length;
  const otherBefore = other.messages.length;

  await api(
    "PUT",
    "/api/meal-pricing",
    tenant.ownerToken,
    pricing
  );

  await until(
    () =>
      own.messages.slice(ownBefore).some(
        (message) =>
          message.notificationType === "MEAL_PRICING" &&
          message.customerId === tenant.customerId
      ),
    "Own customer did not receive the live pricing notification"
  );

  await pause(1500);

  healthy(own);
  healthy(other);

  assert.ok(
    own.messages.slice(ownBefore).every(
      (message) =>
        message.customerId === tenant.customerId
    ),
    "Notification for another customer received"
  );

  assert.equal(
    other.messages.length,
    otherBefore,
    "Other mess customer received this notification"
  );

  const stored = await api(
    "GET",
    "/api/notifications",
    tenant.customerToken
  );

  const live = own.messages.slice(ownBefore).find(
    (message) =>
      message.notificationType === "MEAL_PRICING"
  );

  assert.ok(
    stored.some(
      (message) =>
        message.notificationId === live.notificationId
    ),
    "Live pricing notification missing from persisted history"
  );
}

console.log(
  `Local WebSocket isolation tests: ${base.origin}`
);
console.log("Creating two fresh test messes.");

try {
  const a = await setup("A");
  const b = await setup("B");

  assert.notEqual(
    a.messId,
    b.messId,
    "Setup needs two different messes"
  );

  console.log(
    `Fixtures ready: mess A=${a.messId}, mess B=${b.messId}`
  );

  const ao = await connect(a.ownerToken);
  const bo = await connect(b.ownerToken);
  const ac = await connect(a.customerToken);
  const bc = await connect(b.customerToken);

  await check(
    "Four valid accounts authenticate over STOMP",
    async () => {
      [ao, bo, ac, bc].forEach(healthy);
    }
  );

  subscribe(
    ao,
    `/topic/dashboard/${a.messId}/DINNER`
  );
  subscribe(
    bo,
    `/topic/dashboard/${b.messId}/DINNER`
  );
  subscribe(ac, "/user/queue/notifications");
  subscribe(bc, "/user/queue/notifications");

  // The simple broker has no subscription receipts.
  // Actual message delivery below supplies positive controls.
  await pause(750);

  await check(
    "Mess A dashboard update reaches only owner A",
    () => dashboardChange(a, ao, bo)
  );

  await check(
    "Mess B dashboard update reaches only owner B",
    () => dashboardChange(b, bo, ao)
  );

  await check(
    "Mess A notification reaches only customer A and is persisted",
    () =>
      pricingChange(a, ac, bc, {
        halfMealPrice: 65,
        fullMealPrice: 85,
        extraRotiPrice: 12,
      })
  );

  await check(
    "Mess B notification reaches only customer B and is persisted",
    () =>
      pricingChange(b, bc, ac, {
        halfMealPrice: 70,
        fullMealPrice: 90,
        extraRotiPrice: 15,
      })
  );

  await check(
    "Missing CONNECT token rejected",
    () => rejected(null)
  );

  await check(
    "Malformed CONNECT token rejected",
    () => rejected("invalid.jwt.token")
  );

  await check(
    "Owner A cannot subscribe to mess B dashboard",
    () =>
      rejected(a.ownerToken, (client) =>
        client.subscribe(
          `/topic/dashboard/${b.messId}/DINNER`,
          () => {}
        )
      )
  );

  await check(
    "Owner B cannot subscribe to mess A dashboard",
    () =>
      rejected(b.ownerToken, (client) =>
        client.subscribe(
          `/topic/dashboard/${a.messId}/DINNER`,
          () => {}
        )
      )
  );

  await check(
    "Customer cannot subscribe to own mess owner dashboard",
    () =>
      rejected(a.customerToken, (client) =>
        client.subscribe(
          `/topic/dashboard/${a.messId}/DINNER`,
          () => {}
        )
      )
  );

  await check(
    "Owner cannot subscribe to customer notifications",
    () =>
      rejected(a.ownerToken, (client) =>
        client.subscribe(
          "/user/queue/notifications",
          () => {}
        )
      )
  );

  await check(
    "Legacy shared dashboard topic rejected",
    () =>
      rejected(a.ownerToken, (client) =>
        client.subscribe(
          "/topic/dashboard/DINNER",
          () => {}
        )
      )
  );

  await check(
    "Invalid dashboard meal session rejected",
    () =>
      rejected(a.ownerToken, (client) =>
        client.subscribe(
          `/topic/dashboard/${a.messId}/BREAKFAST`,
          () => {}
        )
      )
  );

  await check(
    "Explicit another-user notification destination rejected",
    () =>
      rejected(a.customerToken, (client) =>
        client.subscribe(
          `/user/${b.customerId}/queue/notifications`,
          () => {}
        )
      )
  );

  for (const destination of [
    "/topic",
    "/queue",
    "/user/queue/notifications",
  ]) {
    await check(
      `Client SEND to ${destination} rejected`,
      () =>
        rejected(a.ownerToken, (client) =>
          client.publish({
            destination,
            body: "{}",
          })
        )
    );
  }

  // Close the customer's existing connection first.
  // These checks verify authentication on a new CONNECT,
  // not revocation of an open connection.
  await stop(ac);

  await api(
    "DELETE",
    `/api/customers/${a.customerId}`,
    a.ownerToken
  );

  try {
    await check(
      "Inactive customer cannot log in",
      () =>
        api(
          "POST",
          "/api/auth/customer/login",
          null,
          { email: a.customerEmail, password },
          400,
          false
        )
    );

    await check(
      "Inactive customer existing token rejected on new STOMP CONNECT",
      () => rejected(a.customerToken)
    );
  } finally {
    const restored = await api(
      "PATCH",
      `/api/customers/${a.customerId}/reactivate`,
      a.ownerToken
    );

    assert.equal(restored.customerId, a.customerId);
    assert.equal(restored.status, "ACTIVE");
  }

  await check(
    "Reactivated customer logs in and reconnects over STOMP",
    async () => {
      const login = await api(
        "POST",
        "/api/auth/customer/login",
        null,
        { email: a.customerEmail, password }
      );

      assert.equal(login.customerId, a.customerId);
      assert.ok(login.accessToken);

      const restoredSocket =
        await connect(login.accessToken);

      healthy(restoredSocket);
      await stop(restoredSocket);
    }
  );
} catch (error) {
  failed++;
  console.error(
    `FAIL  Setup/run stopped: ${error.message}`
  );
} finally {
  for (const tenant of tenants) {
    if (!tenant.windowChanged) continue;

    try {
      await api(
        "PUT",
        "/api/settings/response-window",
        tenant.ownerToken,
        {
          lunchResponseCutoff: "11:00:00",
          dinnerResponseCutoff: "18:00:00",
        }
      );

      tenant.windowChanged = false;
    } catch (error) {
      failed++;
      console.error(
        `FAIL  Restore mess ${tenant.messId} cutoffs: ${error.message}`
      );
    }
  }

  for (const session of sockets) {
    try {
      await stop(session);
    } catch (error) {
      failed++;
      console.error(
        `FAIL  Socket cleanup: ${error.message}`
      );
    }
  }

  console.log(
    `\nRESULT: ${passed} passed, ${failed} failed.`
  );

  console.log(
    "Test fixtures remain in the local database; response cutoffs restored where setup completed."
  );

  console.log(
    "Isolation checks observe live delivery for 1.5 seconds after each positive event."
  );

  process.exitCode = failed ? 1 : 0;
}
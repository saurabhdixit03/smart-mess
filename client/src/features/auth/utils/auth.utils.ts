import type {
  CustomerLoginResponse,
  OwnerLoginResponse,
} from "../types/auth.types";

export type AuthRole = "OWNER" | "CUSTOMER";

interface OwnerSession {
  messOwnerId: number;
  fullName: string;
  messName: string;
  messId: number;
}

interface CustomerSession {
  customerId: number;
  fullName: string;
  mobileNumber: string;
  messName?: string;
}

const AUTH_TOKEN_KEY = "smart_mess_access_token";
const AUTH_ROLE_KEY = "smart_mess_user_role";
const OWNER_KEY = "smart_mess_owner";
const CUSTOMER_KEY = "smart_mess_customer";

export function saveOwnerAuthSession(
  loginResponse: OwnerLoginResponse
): void {
  if (
    !Number.isSafeInteger(loginResponse.messId) ||
    loginResponse.messId <= 0
  ) {
    throw new Error(
      "Your account is not linked to a valid mess."
    );
  }

  localStorage.setItem(
    AUTH_TOKEN_KEY,
    loginResponse.accessToken
  );

  localStorage.setItem(AUTH_ROLE_KEY, "OWNER");

  localStorage.setItem(
    OWNER_KEY,
    JSON.stringify({
      messOwnerId: loginResponse.messOwnerId,
      fullName: loginResponse.fullName,
      messName: loginResponse.messName,
      messId: loginResponse.messId,
    })
  );

  localStorage.removeItem(CUSTOMER_KEY);
}

export function saveCustomerAuthSession(
  loginResponse: CustomerLoginResponse
): void {
  localStorage.setItem(
    AUTH_TOKEN_KEY,
    loginResponse.accessToken
  );

  localStorage.setItem(AUTH_ROLE_KEY, "CUSTOMER");

  localStorage.setItem(
    CUSTOMER_KEY,
    JSON.stringify({
      customerId: loginResponse.customerId,
      fullName: loginResponse.fullName,
      mobileNumber: loginResponse.mobileNumber,
      messName: loginResponse.messName,
    })
  );

  localStorage.removeItem(OWNER_KEY);
}

export function getAccessToken(): string | null {
  return localStorage.getItem(AUTH_TOKEN_KEY);
}

export function getAuthRole(): AuthRole | null {
  const role = localStorage.getItem(AUTH_ROLE_KEY);

  if (role === "OWNER" || role === "CUSTOMER") {
    return role;
  }

  return null;
}

export function getOwner(): OwnerSession | null {
  const storedOwner = localStorage.getItem(OWNER_KEY);

  if (!storedOwner) {
    return null;
  }

  try {
    const owner: OwnerSession = JSON.parse(storedOwner);

    if (
      !owner ||
      !Number.isSafeInteger(owner.messId) ||
      owner.messId <= 0
    ) {
      return null;
    }

    return owner;
  } catch {
    return null;
  }
}

export function getCurrentOwnerMessId(): number | null {
  if (getAuthRole() !== "OWNER") {
    return null;
  }

  return getOwner()?.messId ?? null;
}

export function getCustomer(): CustomerSession | null {
  const storedCustomer =
    localStorage.getItem(CUSTOMER_KEY);

  if (!storedCustomer) {
    return null;
  }

  try {
    return JSON.parse(storedCustomer);
  } catch {
    return null;
  }
}

export function getCurrentCustomerId(): number | null {
  return getCustomer()?.customerId ?? null;
}

export function clearAuthSession(): void {
  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(AUTH_ROLE_KEY);
  localStorage.removeItem(OWNER_KEY);
  localStorage.removeItem(CUSTOMER_KEY);
}

export function isAuthenticated(): boolean {
  return Boolean(getAccessToken());
}
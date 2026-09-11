import {
  Client,
  type StompSubscription,
} from "@stomp/stompjs";
import SockJS from "sockjs-client";

import { getAccessToken } from "@/features/auth/utils/auth.utils";

const WS_URL = import.meta.env.VITE_WS_URL;

export const websocketClient = new Client({
  webSocketFactory: () =>
    new SockJS(WS_URL),

  reconnectDelay: 10000,

  debug: () => {},
});

let connected = false;

/**
 * Multiple parts of the application may depend
 * on the same WebSocket connection.
 *
 * Keep every connection listener instead of
 * replacing websocketClient.onConnect.
 */
const connectionListeners =
  new Set<() => void>();

/**
 * Adds the latest JWT before every
 * WebSocket connection or reconnection.
 */
websocketClient.beforeConnect = () => {
  const token = getAccessToken();

  if (!token) {
    throw new Error(
      "WebSocket authentication token is required."
    );
  }

  websocketClient.connectHeaders = {
    Authorization: `Bearer ${token}`,
  };
};

/**
 * Global connection handler.
 *
 * This is configured once and must not be
 * overwritten by individual hooks/components.
 */
websocketClient.onConnect = () => {
  connected = true;

  console.log(
    "✅ WebSocket Connected"
  );

  connectionListeners.forEach(
    (listener) => {
      listener();
    }
  );
};

websocketClient.onDisconnect = () => {
  connected = false;

  console.log(
    "❌ WebSocket Disconnected"
  );
};

websocketClient.onWebSocketClose = () => {
  connected = false;
};

websocketClient.onStompError = (
  frame
) => {
  connected = false;

  console.error(
    "❌ WebSocket STOMP Error:",
    frame.headers["message"]
  );
};

/**
 * Register a connection listener and
 * ensure the shared WebSocket client
 * is active.
 *
 * If already connected, the listener
 * runs immediately.
 */
export function connectWebSocket(
  onConnected?: () => void
) {
  if (onConnected) {
    connectionListeners.add(
      onConnected
    );
  }

  if (connected) {
    onConnected?.();
    return;
  }

  if (!websocketClient.active) {
    websocketClient.activate();
  }
}

/**
 * Remove a previously registered
 * connection listener.
 */
export function removeWebSocketConnectionListener(
  listener: () => void
) {
  connectionListeners.delete(
    listener
  );
}

/**
 * Disconnect the shared WebSocket client.
 */
export function disconnectWebSocket() {
  if (!websocketClient.active) {
    return;
  }

  websocketClient.deactivate();

  connected = false;

  connectionListeners.clear();
}

/**
 * Subscribe to a topic or
 * user-specific destination.
 */
export function subscribeTopic<T>(
  destination: string,
  callback: (message: T) => void
): StompSubscription {
  return websocketClient.subscribe(
    destination,
    (frame) => {
      callback(
        JSON.parse(frame.body) as T
      );
    }
  );
}
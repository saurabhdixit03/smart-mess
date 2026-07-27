import {
  Client,
  type StompSubscription,
} from "@stomp/stompjs";
import SockJS from "sockjs-client";

const WS_URL = "http://localhost:8080/ws-dashboard";

export const websocketClient = new Client({
  webSocketFactory: () => new SockJS(WS_URL),

  reconnectDelay: 5000,

  debug: () => {},
});

let connected = false;

/**
 * Connect once.
 */
export function connectWebSocket(
  onConnected?: () => void
) {

  if (connected) {

    onConnected?.();

    return;

  }

  websocketClient.onConnect = () => {

    connected = true;

    console.log("✅ WebSocket Connected");

    onConnected?.();

  };

  websocketClient.onDisconnect = () => {

    connected = false;

    console.log("❌ WebSocket Disconnected");

  };

  if (!websocketClient.active) {

    websocketClient.activate();

  }

}

/**
 * Disconnect.
 */
export function disconnectWebSocket() {

  if (!websocketClient.active) {

    return;

  }

  websocketClient.deactivate();

  connected = false;

}

/**
 * Subscribe to a topic.
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
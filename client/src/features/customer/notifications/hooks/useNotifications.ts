import {
  useEffect,
  useRef,
  useState,
} from "react";

import { notificationApi } from "../api";

import type {
  Notification,
} from "../types";

import {
  connectWebSocket,
  removeWebSocketConnectionListener,
  subscribeTopic,
} from "@/services/websocket/websocket.service";

export function useNotifications() {
  const [
    notifications,
    setNotifications,
  ] = useState<Notification[]>([]);

  const [
    loading,
    setLoading,
  ] = useState(true);

  const [
    error,
    setError,
  ] = useState<string | null>(
    null
  );

  const subscriptionRef = useRef<
    ReturnType<typeof subscribeTopic> | null
  >(null);

  /**
   * Load existing notifications.
   */
  const fetchNotifications =
    async () => {
      try {
        setLoading(true);
        setError(null);

        const response =
          await notificationApi
            .getNotifications();

        setNotifications(
          response.data
        );
      } catch {
        setError(
          "Failed to load notifications."
        );
      } finally {
        setLoading(false);
      }
    };

  /**
   * Initial REST fetch +
   * real-time WebSocket subscription.
   */
  useEffect(() => {
    fetchNotifications();

    const handleConnected = () => {
      subscriptionRef.current
        ?.unsubscribe();

      subscriptionRef.current =
        subscribeTopic<Notification>(
          "/user/queue/notifications",
          (notification) => {
            setNotifications(
              (
                currentNotifications
              ) => {
                const alreadyExists =
                  currentNotifications.some(
                    (
                      currentNotification
                    ) =>
                      currentNotification
                        .notificationId ===
                      notification
                        .notificationId
                  );

                if (alreadyExists) {
                  return currentNotifications;
                }

                return [
                  notification,
                  ...currentNotifications,
                ];
              }
            );
          }
        );
    };

    connectWebSocket(
      handleConnected
    );

    return () => {
      subscriptionRef.current
        ?.unsubscribe();

      subscriptionRef.current =
        null;

      removeWebSocketConnectionListener(
        handleConnected
      );
    };
  }, []);

  /**
   * Mark a single notification as read.
   */
  const markAsRead = async (
    notificationId: number
  ) => {
    try {
      const response =
        await notificationApi
          .markAsRead(
            notificationId
          );

      setNotifications(
        (
          currentNotifications
        ) =>
          currentNotifications.map(
            (notification) =>
              notification
                .notificationId ===
              notificationId
                ? response.data
                : notification
          )
      );
    } catch {
      setError(
        "Failed to mark notification as read."
      );
    }
  };

  /**
   * Mark all notifications as read.
   */
  const markAllAsRead =
    async () => {
      try {
        await notificationApi
          .markAllAsRead();

        setNotifications(
          (
            currentNotifications
          ) =>
            currentNotifications.map(
              (notification) => ({
                ...notification,
                read: true,
              })
            )
        );
      } catch {
        setError(
          "Failed to mark notifications as read."
        );
      }
    };

  const unreadCount =
    notifications.filter(
      (notification) =>
        !notification.read
    ).length;

  return {
    notifications,
    unreadCount,
    loading,
    error,
    refresh:
      fetchNotifications,
    markAsRead,
    markAllAsRead,
  };
}
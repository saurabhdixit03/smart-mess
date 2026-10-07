import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { notificationApi } from "../api";

import type { Notification } from "../types";

import {
  connectWebSocket,
  removeWebSocketConnectionListener,
  subscribeTopic,
} from "@/services/websocket/websocket.service";

function getErrorMessage(
  error: unknown,
  fallback: string
): string {
  return error instanceof Error
    ? error.message
    : fallback;
}

function mergeNotifications(
  current: Notification[],
  incoming: Notification[]
): Notification[] {
  const merged = new Map<number, Notification>();

  for (const notification of current) {
    merged.set(
      notification.notificationId,
      notification
    );
  }

  for (const notification of incoming) {
    const existing = merged.get(
      notification.notificationId
    );

    merged.set(notification.notificationId, {
      ...notification,
      read:
        notification.read ||
        existing?.read === true,
    });
  }

  return Array.from(merged.values()).sort(
    (first, second) =>
      second.createdAt.localeCompare(first.createdAt) ||
      second.notificationId - first.notificationId
  );
}

export function useNotifications() {
  const [notifications, setNotifications] =
    useState<Notification[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const mountedRef = useRef(false);
  const requestVersionRef = useRef(0);

  const subscriptionRef = useRef<
    ReturnType<typeof subscribeTopic> | null
  >(null);

  const loadNotifications = useCallback(
    (): Promise<void> => {
      const requestVersion =
        ++requestVersionRef.current;

      return notificationApi
        .getNotifications()
        .then((response) => {
          if (
            !mountedRef.current ||
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setNotifications((current) =>
            mergeNotifications(
              current,
              response.data
            )
          );

          setError(null);
        })
        .catch((err: unknown) => {
          if (
            mountedRef.current &&
            requestVersion === requestVersionRef.current
          ) {
            setError(
              getErrorMessage(
                err,
                "Failed to load notifications."
              )
            );
          }
        })
        .finally(() => {
          if (
            mountedRef.current &&
            requestVersion === requestVersionRef.current
          ) {
            setLoading(false);
          }
        });
    },
    []
  );

  const fetchNotifications = useCallback(
    (): Promise<void> => {
      if (mountedRef.current) {
        setLoading(true);
        setError(null);
      }

      return loadNotifications();
    },
    [loadNotifications]
  );

  useEffect(() => {
    mountedRef.current = true;

    void loadNotifications();

    const handleConnected = () => {
      if (!mountedRef.current) {
        return;
      }

      subscriptionRef.current?.unsubscribe();

      subscriptionRef.current =
        subscribeTopic<Notification>(
          "/user/queue/notifications",
          (notification) => {
            if (!mountedRef.current) {
              return;
            }

            setNotifications((current) =>
              mergeNotifications(
                current,
                [notification]
              )
            );
          }
        );

      // Recover updates missed while disconnected.
      void loadNotifications();
    };

    connectWebSocket(handleConnected);

    return () => {
      mountedRef.current = false;
      requestVersionRef.current += 1;

      subscriptionRef.current?.unsubscribe();
      subscriptionRef.current = null;

      removeWebSocketConnectionListener(
        handleConnected
      );
    };
  }, [loadNotifications]);

  const markAsRead = useCallback(
    async (notificationId: number): Promise<void> => {
      try {
        const response =
          await notificationApi.markAsRead(
            notificationId
          );

        if (!mountedRef.current) {
          return;
        }

        setNotifications((current) =>
          mergeNotifications(
            current,
            [response.data]
          )
        );
      } catch (err) {
        const message = getErrorMessage(
          err,
          "Failed to mark notification as read."
        );

        if (mountedRef.current) {
          setError(message);
        }

        throw new Error(message, { cause: err });
      }
    },
    []
  );

  const markAllAsRead = async (): Promise<void> => {
    const notificationIds = new Set(
      notifications.map(
        (notification) => notification.notificationId
      )
    );

    try {
      await notificationApi.markAllAsRead();

      if (!mountedRef.current) {
        return;
      }

      setNotifications((current) =>
        current.map((notification) =>
          notificationIds.has(
            notification.notificationId
          )
            ? { ...notification, read: true }
            : notification
        )
      );
    } catch (err) {
      const message = getErrorMessage(
        err,
        "Failed to mark notifications as read."
      );

      if (mountedRef.current) {
        setError(message);
      }

      throw new Error(message, { cause: err });
    }
  };

  const unreadCount = notifications.filter(
    (notification) => !notification.read
  ).length;

  return {
    notifications,
    unreadCount,
    loading,
    error,
    refresh: fetchNotifications,
    markAsRead,
    markAllAsRead,
  };
}
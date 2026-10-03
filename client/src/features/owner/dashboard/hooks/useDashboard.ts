import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { dashboardApi } from "../api/dashboard.api";

import {
  connectWebSocket,
  removeWebSocketConnectionListener,
  subscribeTopic,
  websocketClient,
} from "@/services/websocket/websocket.service";

import {
  getCurrentOwnerMessId,
} from "@/features/auth/utils/auth.utils";

import type {
  DashboardSummary,
  MealSession,
} from "../types/dashboard.types";

export function useDashboard(
  mealSession: MealSession,
  enabled = true
) {
  const [dashboard, setDashboard] =
    useState<DashboardSummary | null>(null);

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const subscriptionRef = useRef<
    ReturnType<typeof subscribeTopic> | null
  >(null);

  const messId = getCurrentOwnerMessId();

  const fetchDashboard = useCallback(
    async () => {
      if (!enabled) {
        setDashboard(null);
        setLoading(false);
        setError(null);
        return;
      }

      if (messId === null) {
        setDashboard(null);
        setLoading(false);
        setError(
          "Please sign out and sign in again to load your mess dashboard."
        );
        return;
      }

      try {
        setLoading(true);
        setError(null);

        const response =
          await dashboardApi.getDashboardSummary(
            mealSession
          );

        setDashboard(response.data);
      } catch {
        setError(
          "Failed to load dashboard."
        );
      } finally {
        setLoading(false);
      }
    },
    [mealSession, enabled, messId]
  );

  useEffect(() => {
    if (!enabled) {
      return;
    }

    void fetchDashboard();

    if (messId === null) {
      return;
    }

    let disposed = false;

    /*
     * Each owner subscribes only to their own mess.
     * The backend independently authorizes the destination.
     *
     * This listener also restores the subscription
     * after a WebSocket reconnection.
     */
    const onConnected = () => {
      if (disposed) {
        return;
      }

      /*
       * A reconnected socket has new subscriptions.
       * Do not send an unsubscribe for an old socket's ID.
       */
      subscriptionRef.current =
        subscribeTopic<DashboardSummary>(
          `/topic/dashboard/${messId}/${mealSession}`,
          (updatedDashboard) => {
            if (disposed) {
              return;
            }

            setDashboard(updatedDashboard);
          }
        );
    };

    connectWebSocket(onConnected);

    return () => {
      disposed = true;

      removeWebSocketConnectionListener(
        onConnected
      );

      if (websocketClient.connected) {
        subscriptionRef.current?.unsubscribe();
      }

      subscriptionRef.current = null;
    };
  }, [
    fetchDashboard,
    mealSession,
    enabled,
    messId,
  ]);

  return {
    dashboard,
    loading,
    error,
    refresh: fetchDashboard,
  };
}
import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { dashboardApi } from "../api/dashboard.api";

import {
  connectWebSocket,
  subscribeTopic,
} from "@/services/websocket/websocket.service";

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

  const fetchDashboard = useCallback(
    async () => {
      if (!enabled) {
        setDashboard(null);
        setLoading(false);
        setError(null);
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
    [mealSession, enabled]
  );

  useEffect(() => {
    if (!enabled) {
      subscriptionRef.current?.unsubscribe();
      subscriptionRef.current = null;

      return;
    }

    fetchDashboard();

    connectWebSocket(() => {
      subscriptionRef.current?.unsubscribe();

      subscriptionRef.current =
        subscribeTopic<DashboardSummary>(
          `/topic/dashboard/${mealSession}`,
          (dashboard) => {
            console.log(
              "📡 Dashboard Update",
              dashboard
            );

            setDashboard(dashboard);
          }
        );
    });

    return () => {
      subscriptionRef.current?.unsubscribe();
      subscriptionRef.current = null;
    };
  }, [fetchDashboard, mealSession, enabled]);

  return {
    dashboard,
    loading,
    error,
    refresh: fetchDashboard,
  };
}
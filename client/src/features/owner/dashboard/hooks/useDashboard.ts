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

type DashboardState = {
  key: string;
  dashboard: DashboardSummary | null;
  loading: boolean;
  error: string | null;
};

export function useDashboard(
  mealSession: MealSession,
  enabled = true
) {
  const [state, setState] =
    useState<DashboardState | null>(null);

  const requestVersionRef = useRef(0);

  const subscriptionRef = useRef<
    ReturnType<typeof subscribeTopic> | null
  >(null);

  const messId = getCurrentOwnerMessId();
  const stateKey =
    `${messId}-${mealSession}-${enabled}`;

  const loadDashboard = useCallback(
    (): Promise<void> => {
      const version = ++requestVersionRef.current;

      if (!enabled || messId === null) {
        return Promise.resolve();
      }

      return dashboardApi
        .getDashboardSummary(mealSession)
        .then((response) => {
          if (requestVersionRef.current !== version) {
            return;
          }

          setState({
            key: stateKey,
            dashboard: response.data,
            loading: false,
            error: null,
          });
        })
        .catch(() => {
          if (requestVersionRef.current !== version) {
            return;
          }

          setState((previous) => ({
            key: stateKey,
            dashboard:
              previous?.key === stateKey
                ? previous.dashboard
                : null,
            loading: false,
            error: "Failed to load dashboard.",
          }));
        });
    },
    [mealSession, enabled, messId, stateKey]
  );

  const fetchDashboard = useCallback(
    (): Promise<void> => {
      if (!enabled || messId === null) {
        setState(null);
      } else {
        setState((previous) => ({
          key: stateKey,
          dashboard:
            previous?.key === stateKey
              ? previous.dashboard
              : null,
          loading: true,
          error: null,
        }));
      }

      return loadDashboard();
    },
    [enabled, messId, stateKey, loadDashboard]
  );

  useEffect(() => {
    if (!enabled || messId === null) {
      return;
    }

    let disposed = false;

    void loadDashboard();

    /*
     * Each owner subscribes only to their own mess.
     * The backend independently authorizes the destination.
     * Restore the subscription after reconnecting.
     */
    const onConnected = () => {
      if (disposed) {
        return;
      }

      /*
       * A reconnected socket has new subscriptions.
       * Do not unsubscribe using an old socket's ID.
       */
      subscriptionRef.current =
        subscribeTopic<DashboardSummary>(
          `/topic/dashboard/${messId}/${mealSession}`,
          (updatedDashboard) => {
            if (disposed) {
              return;
            }

            setState((previous) => ({
              key: stateKey,
              dashboard: updatedDashboard,
              loading:
                previous?.key === stateKey
                  ? previous.loading
                  : true,
              error:
                previous?.key === stateKey
                  ? previous.error
                  : null,
            }));
          }
        );
    };

    connectWebSocket(onConnected);

    return () => {
      disposed = true;
      requestVersionRef.current += 1;

      removeWebSocketConnectionListener(
        onConnected
      );

      if (websocketClient.connected) {
        subscriptionRef.current?.unsubscribe();
      }

      subscriptionRef.current = null;
    };
  }, [
    loadDashboard,
    mealSession,
    enabled,
    messId,
    stateKey,
  ]);

  const currentState =
    state?.key === stateKey ? state : null;

  return {
    dashboard:
      enabled && messId !== null
        ? currentState?.dashboard ?? null
        : null,

    loading:
      enabled &&
      messId !== null &&
      (currentState === null || currentState.loading),

    error:
      !enabled
        ? null
        : messId === null
          ? "Please sign out and sign in again to load your mess dashboard."
          : currentState?.error ?? null,

    refresh: fetchDashboard,
  };
}
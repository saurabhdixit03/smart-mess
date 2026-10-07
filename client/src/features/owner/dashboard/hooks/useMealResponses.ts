import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { dashboardApi } from "../api/dashboard.api";

import type {
  MealResponse,
} from "../types/dashboard.types";

const REFRESH_INTERVAL = 5000;

type ResponsesState = {
  menuId: number;
  mealResponses: MealResponse[];
  error: string | null;
};

export function useMealResponses(menuId?: number) {
  const [state, setState] =
    useState<ResponsesState | null>(null);

  const [refreshing, setRefreshing] =
    useState(false);

  const requestVersionRef = useRef(0);

  const loadMealResponses = useCallback(
    (): Promise<void> => {
      const version = ++requestVersionRef.current;

      if (!menuId) {
        return Promise.resolve();
      }

      return dashboardApi
        .getResponsesByMenu(menuId)
        .then((response) => {
          if (requestVersionRef.current !== version) {
            return;
          }

          setState({
            menuId,
            mealResponses: response.data,
            error: null,
          });
        })
        .catch(() => {
          if (requestVersionRef.current !== version) {
            return;
          }

          setState((previous) => ({
            menuId,
            mealResponses:
              previous?.menuId === menuId
                ? previous.mealResponses
                : [],
            error: "Failed to load meal responses.",
          }));
        })
        .finally(() => {
          if (requestVersionRef.current === version) {
            setRefreshing(false);
          }
        });
    },
    [menuId]
  );

  const refresh = useCallback((): Promise<void> => {
    if (!menuId) {
      setRefreshing(false);
      return loadMealResponses();
    }

    setRefreshing(true);

    setState((previous) =>
      previous?.menuId === menuId
        ? { ...previous, error: null }
        : previous
    );

    return loadMealResponses();
  }, [menuId, loadMealResponses]);

  useEffect(() => {
    if (!menuId) {
      return;
    }

    void loadMealResponses();

    const interval = setInterval(() => {
      void loadMealResponses();
    }, REFRESH_INTERVAL);

    return () => {
      clearInterval(interval);
      requestVersionRef.current += 1;
    };
  }, [menuId, loadMealResponses]);

  const currentState =
    state?.menuId === menuId ? state : null;

  return {
    mealResponses:
      menuId
        ? currentState?.mealResponses ?? []
        : [],

    loading:
      Boolean(menuId) &&
      (currentState === null || refreshing),

    error:
      menuId
        ? currentState?.error ?? null
        : null,

    refresh,
  };
}
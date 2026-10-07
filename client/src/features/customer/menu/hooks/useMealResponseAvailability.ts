import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { mealResponseApi } from "../api";

import type {
  MealResponseAvailability,
} from "../types";

type AvailabilityState = {
  menuId: number;
  availability: MealResponseAvailability | null;
  error: string | null;
};

export function useMealResponseAvailability(
  menuId: number
) {
  const [state, setState] =
    useState<AvailabilityState | null>(null);

  const [refreshing, setRefreshing] =
    useState(false);

  const requestVersionRef = useRef(0);

  const loadAvailability = useCallback(
    (): Promise<void> => {
      const requestVersion =
        ++requestVersionRef.current;

      return mealResponseApi
        .getResponseAvailability(menuId)
        .then((response) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setState({
            menuId,
            availability: response.data,
            error: null,
          });
        })
        .catch((err: unknown) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setState((previous) => ({
            menuId,
            availability:
              previous !== null &&
              previous.menuId === menuId
                ? previous.availability
                : null,
            error:
              err instanceof Error
                ? err.message
                : "Failed to load response availability.",
          }));
        })
        .finally(() => {
          if (
            requestVersion === requestVersionRef.current
          ) {
            setRefreshing(false);
          }
        });
    },
    [menuId]
  );

  const fetchAvailability = useCallback(
    (): Promise<void> => {
      setRefreshing(true);

      setState((previous) =>
        previous !== null &&
        previous.menuId === menuId
          ? { ...previous, error: null }
          : previous
      );

      return loadAvailability();
    },
    [menuId, loadAvailability]
  );

  useEffect(() => {
    void loadAvailability();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadAvailability]);

  const currentState =
    state !== null &&
    state.menuId === menuId
      ? state
      : null;

  return {
    availability: currentState?.availability ?? null,
    loading: currentState === null || refreshing,
    error: currentState?.error ?? null,
    refetch: fetchAvailability,
  };
}
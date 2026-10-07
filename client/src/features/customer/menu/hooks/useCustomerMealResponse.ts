import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { mealResponseApi } from "../api";

import type { MealResponse } from "../types";

type ResponseState = {
  customerId: number;
  menuId: number;
  mealResponse: MealResponse | null;
  error: string | null;
};

export function useCustomerMealResponse(
  customerId: number,
  menuId: number
) {
  const [state, setState] =
    useState<ResponseState | null>(null);

  const [refreshing, setRefreshing] =
    useState(false);

  const requestVersionRef = useRef(0);

  const loadMealResponse = useCallback(
    (): Promise<void> => {
      const requestVersion =
        ++requestVersionRef.current;

      return mealResponseApi
        .getCustomerMealResponse(customerId, menuId)
        .then((response) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setState({
            customerId,
            menuId,
            mealResponse: response.data,
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
            customerId,
            menuId,
            mealResponse:
              previous !== null &&
              previous.customerId === customerId &&
              previous.menuId === menuId
                ? previous.mealResponse
                : null,
            error:
              err instanceof Error
                ? err.message
                : "Failed to load meal response.",
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
    [customerId, menuId]
  );

  const fetchMealResponse = useCallback(
    (): Promise<void> => {
      setRefreshing(true);

      setState((previous) =>
        previous !== null &&
        previous.customerId === customerId &&
        previous.menuId === menuId
          ? { ...previous, error: null }
          : previous
      );

      return loadMealResponse();
    },
    [customerId, menuId, loadMealResponse]
  );

  useEffect(() => {
    void loadMealResponse();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadMealResponse]);

  const currentState =
    state !== null &&
    state.customerId === customerId &&
    state.menuId === menuId
      ? state
      : null;

  return {
    mealResponse: currentState?.mealResponse ?? null,
    loading: currentState === null || refreshing,
    error: currentState?.error ?? null,
    refetch: fetchMealResponse,
  };
}
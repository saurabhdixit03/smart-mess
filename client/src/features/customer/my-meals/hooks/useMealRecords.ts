import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { mealRecordApi } from "../api";

import type { MealRecord } from "../types";

type MealHistoryState = {
  customerId: number;
  mealRecords: MealRecord[];
  error: string | null;
};

export function useMealRecords(
  customerId: number
) {
  const [history, setHistory] =
    useState<MealHistoryState | null>(null);

  const [refreshing, setRefreshing] =
    useState(false);

  const requestVersionRef = useRef(0);

  const loadMealRecords = useCallback(
    (): Promise<void> => {
      const requestVersion =
        ++requestVersionRef.current;

      return mealRecordApi
        .getCustomerMealHistory(customerId)
        .then((response) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setHistory({
            customerId,
            mealRecords: response.data,
            error: null,
          });
        })
        .catch(() => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setHistory((previous) => ({
            customerId,
            mealRecords:
              previous !== null &&
              previous.customerId === customerId
                ? previous.mealRecords
                : [],
            error: "Failed to load meal history.",
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
    [customerId]
  );

  const fetchMealRecords = useCallback(
    (): Promise<void> => {
      setRefreshing(true);

      setHistory((previous) =>
        previous !== null &&
        previous.customerId === customerId
          ? { ...previous, error: null }
          : previous
      );

      return loadMealRecords();
    },
    [customerId, loadMealRecords]
  );

  useEffect(() => {
    void loadMealRecords();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadMealRecords]);

  const currentHistory =
    history !== null &&
    history.customerId === customerId
      ? history
      : null;

  return {
    mealRecords: currentHistory?.mealRecords ?? [],
    loading: currentHistory === null || refreshing,
    error: currentHistory?.error ?? null,
    fetchMealRecords,
  };
}
import {
  useCallback,
  useEffect,
  useState,
} from "react";

import { getTodayMealRecords } from "../api";

import type {
  MealSession,
  TodayMealRecord,
} from "../types";

export function useTodayMealRecords(
  mealSession: MealSession,
  enabled = true
) {
  const [
    mealRecords,
    setMealRecords,
  ] = useState<TodayMealRecord[]>([]);

  const [
    loading,
    setLoading,
  ] = useState(false);

  const [
    error,
    setError,
  ] = useState<string | null>(null);

  const fetchTodayMealRecords =
    useCallback(async () => {
      if (!enabled) {
        setMealRecords([]);
        setLoading(false);
        setError(null);
        return;
      }

      try {
        setLoading(true);
        setError(null);

        const response =
          await getTodayMealRecords(
            mealSession
          );

        setMealRecords(
          response.data
        );
      } catch {
        setError(
          "Failed to load today's meal records."
        );
      } finally {
        setLoading(false);
      }
    }, [mealSession, enabled]);

  useEffect(() => {
    if (!enabled) {
      setMealRecords([]);
      setLoading(false);
      setError(null);
      return;
    }

    fetchTodayMealRecords();
  }, [
    fetchTodayMealRecords,
    enabled,
  ]);

  return {
    mealRecords,
    loading,
    error,
    refetch: fetchTodayMealRecords,
  };
}
import { useCallback, useEffect, useState } from "react";

import { getTodayMealRecords } from "../api";

import type {
  MealSession,
  TodayMealRecord,
} from "../types";

export function useTodayMealRecords(
  mealSession: MealSession
) {

  const [
    mealRecords,
    setMealRecords,
  ] = useState<TodayMealRecord[]>([]);

  const [
    loading,
    setLoading,
  ] = useState(true);

  const [
    error,
    setError,
  ] = useState<string | null>(null);

  const fetchTodayMealRecords =
    useCallback(async () => {

      try {

        setLoading(true);

        const response =
          await getTodayMealRecords(
            mealSession
          );

        setMealRecords(
          response.data
        );

        setError(null);

      } catch {

        setError(
          "Failed to load today's meal records."
        );

      } finally {

        setLoading(false);

      }

    }, [mealSession]);

  useEffect(() => {

    fetchTodayMealRecords();

  }, [fetchTodayMealRecords]);

  return {

    mealRecords,

    loading,

    error,

    refetch: fetchTodayMealRecords,

  };

}
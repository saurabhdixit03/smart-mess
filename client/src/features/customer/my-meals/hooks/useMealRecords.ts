import { useEffect, useState } from "react";

import { mealRecordApi } from "../api";
import type { MealRecord } from "../types";

export function useMealRecords(
  customerId: number
) {
  const [mealRecords, setMealRecords] =
    useState<MealRecord[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const fetchMealRecords =
    async () => {

      try {
        setLoading(true);
        setError(null);

        const apiResponse =
          await mealRecordApi.getCustomerMealHistory(
            customerId
          );

        setMealRecords(
          apiResponse.data
        );

      } catch {

        setError(
          "Failed to load meal history."
        );

      } finally {

        setLoading(false);

      }

    };

  useEffect(() => {
    fetchMealRecords();
  }, [customerId]);

  return {
    mealRecords,
    loading,
    error,
    fetchMealRecords,
  };
}
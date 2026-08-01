import { useCallback, useEffect, useState } from "react";

import { mealResponseApi } from "../api";

import type { MealResponse } from "../types";

export function useCustomerMealResponse(
  customerId: number,
  menuId: number
) {
  const [mealResponse, setMealResponse] =
    useState<MealResponse | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const fetchMealResponse =
    useCallback(async () => {
      try {
        setLoading(true);
        setError(null);

        const response =
          await mealResponseApi.getCustomerMealResponse(
            customerId,
            menuId
          );

        setMealResponse(response.data);
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Failed to load meal response."
        );
      } finally {
        setLoading(false);
      }
    }, [customerId, menuId]);

  useEffect(() => {
    fetchMealResponse();
  }, [fetchMealResponse]);

  return {
    mealResponse,
    loading,
    error,
    refetch: fetchMealResponse,
  };
}
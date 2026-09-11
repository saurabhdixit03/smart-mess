import { useCallback, useEffect, useState } from "react";

import { mealResponseApi } from "../api";

import type {
  MealResponseAvailability,
} from "../types";

export function useMealResponseAvailability(
  menuId: number
) {
  const [availability, setAvailability] =
    useState<MealResponseAvailability | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const fetchAvailability =
    useCallback(async () => {
      try {
        setLoading(true);
        setError(null);

        const response =
          await mealResponseApi.getResponseAvailability(
            menuId
          );

        setAvailability(response.data);

      } catch (err) {

        setError(
          err instanceof Error
            ? err.message
            : "Failed to load response availability."
        );

      } finally {

        setLoading(false);

      }
    }, [menuId]);

  useEffect(() => {
    fetchAvailability();
  }, [fetchAvailability]);

  return {
    availability,
    loading,
    error,
    refetch: fetchAvailability,
  };
}
import { useCallback, useEffect, useState } from "react";

import { dashboardApi } from "../api/dashboard.api";

import type { MealResponse } from "../types/dashboard.types";

const REFRESH_INTERVAL = 5000;

export function useMealResponses(menuId?: number) {

  const [mealResponses, setMealResponses] = useState<
    MealResponse[]
  >([]);

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const fetchMealResponses = useCallback(
    async (showLoader = true) => {

      if (!menuId) {
        setMealResponses([]);
        return;
      }

      try {

        if (showLoader) {
          setLoading(true);
        }

        setError(null);

        const response =
          await dashboardApi.getResponsesByMenu(
            menuId
          );

        setMealResponses(response.data);

      } catch {

        setError(
          "Failed to load meal responses."
        );

      } finally {

        if (showLoader) {
          setLoading(false);
        }

      }

    },
    [menuId]
  );

  useEffect(() => {

    fetchMealResponses(true);

    const interval = setInterval(() => {
      fetchMealResponses(false);
    }, REFRESH_INTERVAL);

    return () => clearInterval(interval);

  }, [fetchMealResponses]);

  return {

    mealResponses,

    loading,

    error,

    refresh: () =>
      fetchMealResponses(true),

  };

}
import { useCallback, useEffect, useState } from "react";

import { insightsApi } from "../api";

import type {
  MonthlyInsightsResponse,
} from "../types";

export function useInsights(
  initialMonth: number,
  initialYear: number
) {

  const [insights, setInsights] =
    useState<MonthlyInsightsResponse | null>(
      null
    );

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const fetchInsights =
    useCallback(
      async (
        month: number = initialMonth,
        year: number = initialYear
      ) => {

        try {

          setLoading(true);

          setError(null);

          const response =
            await insightsApi.getMonthlyInsights(
              month,
              year
            );

          setInsights(response.data);

        } catch {

          setError(
            "Failed to load insights."
          );

        } finally {

          setLoading(false);

        }

      },
      [initialMonth, initialYear]
    );

  useEffect(() => {

    fetchInsights(
      initialMonth,
      initialYear
    );

  }, [
    fetchInsights,
    initialMonth,
    initialYear,
  ]);

  return {

    insights,

    loading,

    error,

    fetchInsights,

  };

}
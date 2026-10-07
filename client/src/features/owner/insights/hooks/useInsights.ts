import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { insightsApi } from "../api";

import type {
  MonthlyInsightsResponse,
} from "../types";

export function useInsights(
  initialMonth: number,
  initialYear: number
) {
  const [insights, setInsights] =
    useState<MonthlyInsightsResponse | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const requestVersionRef = useRef(0);

  const loadInsights = useCallback(
    (
      month: number,
      year: number
    ): Promise<void> => {
      const version = ++requestVersionRef.current;

      return insightsApi
        .getMonthlyInsights(month, year)
        .then((response) => {
          if (requestVersionRef.current !== version) {
            return;
          }

          setInsights(response.data);
          setError(null);
        })
        .catch(() => {
          if (requestVersionRef.current === version) {
            setError("Failed to load insights.");
          }
        })
        .finally(() => {
          if (requestVersionRef.current === version) {
            setLoading(false);
          }
        });
    },
    []
  );

  const fetchInsights = useCallback(
    (
      month: number = initialMonth,
      year: number = initialYear
    ): Promise<void> => {
      setLoading(true);
      setError(null);

      return loadInsights(month, year);
    },
    [initialMonth, initialYear, loadInsights]
  );

  useEffect(() => {
    void loadInsights(initialMonth, initialYear);

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadInsights, initialMonth, initialYear]);

  return {
    insights,
    loading,
    error,
    fetchInsights,
  };
}
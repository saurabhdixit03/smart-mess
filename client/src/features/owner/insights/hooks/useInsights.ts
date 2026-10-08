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

type InsightsState = {
  month: number;
  year: number;
  insights: MonthlyInsightsResponse | null;
  loading: boolean;
  error: string | null;
};

export function useInsights(
  month: number,
  year: number
) {
  const requestVersionRef = useRef(0);

  const [state, setState] = useState<InsightsState>({
    month,
    year,
    insights: null,
    loading: true,
    error: null,
  });

  const loadInsights = useCallback(
    (): Promise<void> => {
      const version = ++requestVersionRef.current;

      return insightsApi
        .getMonthlyInsights(month, year)
        .then((response) => {
          if (version !== requestVersionRef.current) {
            return;
          }

          setState({
            month,
            year,
            insights: response.data,
            loading: false,
            error: null,
          });
        })
        .catch((error: unknown) => {
          if (version !== requestVersionRef.current) {
            return;
          }

          setState({
            month,
            year,
            insights: null,
            loading: false,
            error:
              error instanceof Error
                ? error.message
                : "Unable to load insights. Please try again.",
          });
        });
    },
    [month, year]
  );

  const fetchInsights = useCallback(
    (): Promise<void> => {
      setState({
        month,
        year,
        insights: null,
        loading: true,
        error: null,
      });

      return loadInsights();
    },
    [month, year, loadInsights]
  );

  useEffect(() => {
    void loadInsights();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadInsights]);

  const matchesPeriod =
    state.month === month &&
    state.year === year;

  return {
    insights: matchesPeriod ? state.insights : null,
    loading: !matchesPeriod || state.loading,
    error: matchesPeriod ? state.error : null,
    fetchInsights,
  };
}
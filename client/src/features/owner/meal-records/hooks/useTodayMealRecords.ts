import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { getTodayMealRecords } from "../api";

import type {
  MealSession,
  TodayMealRecord,
} from "../types";

type MealRecordsState = {
  key: string;
  mealRecords: TodayMealRecord[];
  loading: boolean;
  error: string | null;
};

export function useTodayMealRecords(
  mealSession: MealSession,
  enabled = true
) {
  const [state, setState] =
    useState<MealRecordsState | null>(null);

  const requestVersionRef = useRef(0);

  const stateKey = `${mealSession}-${enabled}`;

  const loadTodayMealRecords = useCallback(
    (): Promise<void> => {
      const version = ++requestVersionRef.current;

      if (!enabled) {
        return Promise.resolve();
      }

      return getTodayMealRecords(mealSession)
        .then((response) => {
          if (requestVersionRef.current !== version) {
            return;
          }

          setState({
            key: stateKey,
            mealRecords: response.data,
            loading: false,
            error: null,
          });
        })
        .catch(() => {
          if (requestVersionRef.current !== version) {
            return;
          }

          setState((previous) => ({
            key: stateKey,
            mealRecords:
              previous?.key === stateKey
                ? previous.mealRecords
                : [],
            loading: false,
            error: "Failed to load today's meal records.",
          }));
        });
    },
    [mealSession, enabled, stateKey]
  );

  const refetch = useCallback((): Promise<void> => {
    if (!enabled) {
      setState(null);
    } else {
      setState((previous) => ({
        key: stateKey,
        mealRecords:
          previous?.key === stateKey
            ? previous.mealRecords
            : [],
        loading: true,
        error: null,
      }));
    }

    return loadTodayMealRecords();
  }, [enabled, stateKey, loadTodayMealRecords]);

  useEffect(() => {
    if (!enabled) {
      return;
    }

    void loadTodayMealRecords();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [enabled, loadTodayMealRecords]);

  const currentState =
    state?.key === stateKey ? state : null;

  return {
    mealRecords:
      enabled
        ? currentState?.mealRecords ?? []
        : [],

    loading:
      enabled &&
      (currentState === null || currentState.loading),

    error:
      enabled
        ? currentState?.error ?? null
        : null,

    refetch,
  };
}
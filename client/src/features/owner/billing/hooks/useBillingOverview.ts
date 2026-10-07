import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { billingApi } from "../api";

import type { BillingOverviewResponse } from "../types";

type OverviewState = {
  period: string;
  data: BillingOverviewResponse | null;
  loading: boolean;
  error: string | null;
};

export function useBillingOverview(
  billingMonth: number | null = null,
  billingYear: number | null = null
) {
  const period =
    billingMonth === null && billingYear === null
      ? "ALL"
      : `${billingYear}-${billingMonth}`;

  const [state, setState] =
    useState<OverviewState | null>(null);

  const requestVersionRef = useRef(0);

  const loadBillingOverview = useCallback(
    (): Promise<void> => {
      const requestVersion =
        ++requestVersionRef.current;

      return Promise.resolve()
        .then(() =>
          billingApi.getBillingOverview(
            billingMonth,
            billingYear
          )
        )
        .then((response) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setState({
            period,
            data: response.data,
            loading: false,
            error: null,
          });
        })
        .catch((err: unknown) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          const message =
            err instanceof Error
              ? err.message
              : "Failed to load billing overview.";

          setState((previous) => ({
            period,
            data:
              previous?.period === period
                ? previous.data
                : null,
            loading: false,
            error: message,
          }));

          throw new Error(message, { cause: err });
        });
    },
    [billingMonth, billingYear, period]
  );

  const refreshBillingOverview = useCallback(
    (): Promise<void> => {
      setState((previous) => ({
        period,
        data:
          previous?.period === period
            ? previous.data
            : null,
        loading: true,
        error: null,
      }));

      return loadBillingOverview();
    },
    [loadBillingOverview, period]
  );

  useEffect(() => {
    void loadBillingOverview().catch(() => {
      // The error is exposed through hook state.
    });

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadBillingOverview]);

  const currentState =
    state?.period === period ? state : null;

  return {
    overview: currentState?.data ?? null,
    loading: currentState?.loading ?? true,
    error: currentState?.error ?? null,
    refreshBillingOverview,
  };
}
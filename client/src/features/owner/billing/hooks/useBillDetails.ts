import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { billingApi } from "../api";

import type { BillDetailResponse } from "../types";

type DetailsState = {
  billId: number;
  bill: BillDetailResponse | null;
  loading: boolean;
  error: string | null;
};

export function useBillDetails(
  billId: number | null
) {
  const [state, setState] =
    useState<DetailsState | null>(null);

  const requestVersionRef = useRef(0);

  const loadBillDetails = useCallback(
    (): Promise<void> => {
      const requestVersion =
        ++requestVersionRef.current;

      if (billId === null) {
        return Promise.resolve();
      }

      return billingApi
        .getBillDetails(billId)
        .then((response) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setState({
            billId,
            bill: response.data,
            loading: false,
            error: null,
          });
        })
        .catch((error: unknown) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setState({
            billId,
            bill: null,
            loading: false,
            error:
              error instanceof Error
                ? error.message
                : "Failed to load bill details.",
          });
        });
    },
    [billId]
  );

  const refreshBillDetails = useCallback(
    (): Promise<void> => {
      if (billId === null) {
        setState(null);
      } else {
        setState({
          billId,
          bill: null,
          loading: true,
          error: null,
        });
      }

      return loadBillDetails();
    },
    [billId, loadBillDetails]
  );

  useEffect(() => {
    void loadBillDetails();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadBillDetails]);

  const currentState =
    billId !== null && state?.billId === billId
      ? state
      : null;

  return {
    bill: currentState?.bill ?? null,

    loading:
      billId !== null &&
      (currentState === null || currentState.loading),

    error: currentState?.error ?? null,

    refreshBillDetails,
  };
}
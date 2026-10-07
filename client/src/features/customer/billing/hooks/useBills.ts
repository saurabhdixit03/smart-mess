import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { billingApi } from "../api";

import type { Bill } from "../types";

export function useBills() {
  const [bills, setBills] =
    useState<Bill[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const requestVersionRef = useRef(0);

  const loadBills = useCallback(
    (): Promise<void> => {
      const requestVersion =
        ++requestVersionRef.current;

      return billingApi
        .getCustomerBills()
        .then((response) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setBills(response.data);
          setError(null);
        })
        .catch(() => {
          if (
            requestVersion === requestVersionRef.current
          ) {
            setError("Failed to load bills.");
          }
        })
        .finally(() => {
          if (
            requestVersion === requestVersionRef.current
          ) {
            setLoading(false);
          }
        });
    },
    []
  );

  const fetchBills = useCallback(
    (): Promise<void> => {
      setLoading(true);
      setError(null);

      return loadBills();
    },
    [loadBills]
  );

  useEffect(() => {
    void loadBills();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadBills]);

  return {
    bills,
    loading,
    error,
    fetchBills,
  };
}
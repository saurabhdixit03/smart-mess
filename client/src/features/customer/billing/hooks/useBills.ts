import {
  useCallback,
  useEffect,
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

  const fetchBills =
    useCallback(async () => {
      try {
        setLoading(true);

        setError(null);

        const response =
          await billingApi.getCustomerBills();

        setBills(response.data);
      } catch {
        setError(
          "Failed to load bills."
        );
      } finally {
        setLoading(false);
      }
    }, []);

  useEffect(() => {
    void fetchBills();
  }, [fetchBills]);

  return {
    bills,
    loading,
    error,
    fetchBills,
  };
}
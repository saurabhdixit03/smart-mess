import { useEffect, useState } from "react";

import { billingApi } from "../api";

import type { Bill } from "../types";

export function useBills(
  customerId: number
) {
  const [bills, setBills] = useState<Bill[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const fetchBills = async () => {
    try {
      setLoading(true);

      setError(null);

      const response =
        await billingApi.getCustomerBills(
          customerId
        );

      setBills(response.data);

    } catch {

      setError(
        "Failed to load bills."
      );

    } finally {

      setLoading(false);

    }
  };

  useEffect(() => {
    fetchBills();
  }, [customerId]);

  return {
    bills,
    loading,
    error,
    fetchBills,
  };
}
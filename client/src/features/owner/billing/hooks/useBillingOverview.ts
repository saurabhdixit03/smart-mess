import { useCallback, useEffect, useState } from "react";

import { billingApi } from "../api";

import type { BillingOverviewResponse } from "../types";

export function useBillingOverview(
  billingMonth: number,
  billingYear: number
) {
  const [overview, setOverview] =
    useState<BillingOverviewResponse | null>(null);

  const [loading, setLoading] =
    useState(true);

  const fetchBillingOverview =
    useCallback(async () => {
      try {
        setLoading(true);

        const response =
          await billingApi.getBillingOverview(
            billingMonth,
            billingYear
          );

        setOverview(response.data);

      } finally {
        setLoading(false);
      }
    }, [billingMonth, billingYear]);

  useEffect(() => {
    fetchBillingOverview();
  }, [fetchBillingOverview]);

  return {
    overview,
    loading,
    refreshBillingOverview:
      fetchBillingOverview,
  };
}
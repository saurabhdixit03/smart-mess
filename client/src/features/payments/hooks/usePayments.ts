import { useCallback, useEffect, useState } from "react";

import { paymentApi } from "../api";    

import type { PaymentOverviewResponse } from "../types";

export function usePayments() {
  const [overview, setOverview] =
    useState<PaymentOverviewResponse | null>(
      null
    );

  const [loading, setLoading] =
    useState(false);

  const fetchPayments =
    useCallback(async () => {
      try {
        setLoading(true);
const apiResponse =
  await paymentApi.getPaymentOverview();

setOverview(apiResponse.data);

      } finally {
        setLoading(false);
      }
    }, []);

  useEffect(() => {
    fetchPayments();
  }, [fetchPayments]);

  return {
    overview,
    loading,

    refreshPayments:
      fetchPayments,
  };
}
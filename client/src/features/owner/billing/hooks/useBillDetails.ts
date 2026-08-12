import { useCallback, useEffect, useState } from "react";

import { billingApi } from "../api";

import type { BillDetailResponse } from "../types";

export function useBillDetails(
  billId: number | null
) {
  const [bill, setBill] =
    useState<BillDetailResponse | null>(null);

  const [loading, setLoading] =
    useState(false);

  const fetchBillDetails =
    useCallback(async () => {
      if (billId === null) {
        setBill(null);
        return;
      }

      try {
        setLoading(true);

        const response =
          await billingApi.getBillDetails(
            billId
          );

        setBill(response.data);

      } finally {
        setLoading(false);
      }
    }, [billId]);

  useEffect(() => {
    fetchBillDetails();
  }, [fetchBillDetails]);

  return {
    bill,
    loading,
    refreshBillDetails:
      fetchBillDetails,
  };
}
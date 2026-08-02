import { useCallback, useState } from "react";

import { billingApi } from "../api";

import type { BillDetail } from "../types";

export function useBillDetails() {
  const [billDetail, setBillDetail] =
    useState<BillDetail | null>(null);

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const fetchBillDetails = useCallback(
    async (billId: number) => {
      try {
        setLoading(true);
        setError(null);

        console.log("Fetching bill:", billId);

        const response =
          await billingApi.getBillDetails(billId);

        console.log("API Response:", response);

        setBillDetail(response.data);

        console.log("Stored bill:", response.data);

      } catch (err) {

        console.error(err);

        setError(
          "Failed to load bill details."
        );

      } finally {

        setLoading(false);

      }
    },
    []
  );

  return {
    billDetail,
    loading,
    error,
    fetchBillDetails,
  };
}
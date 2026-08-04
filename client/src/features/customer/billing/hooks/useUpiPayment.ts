import { useCallback, useState } from "react";

import { paymentApi } from "../api";

import type { UpiPayment } from "../types";

export function useUpiPayment() {
  const [payment, setPayment] =
    useState<UpiPayment | null>(null);

  const [loading, setLoading] =
    useState(false);

  const [submitting, setSubmitting] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const fetchUpiPayment = useCallback(
    async (billId: number) => {
      try {
        setLoading(true);
        setError(null);

        const response =
          await paymentApi.generateUpiPayment(
            billId
          );

        setPayment(response.data);

      } catch (err) {
        console.error(err);

        setError(
          "Failed to load payment details."
        );

      } finally {
        setLoading(false);
      }
    },
    []
  );

  const requestVerification =
    useCallback(
      async (
        billId: number
      ) => {
        try {
          setSubmitting(true);
          setError(null);

          await paymentApi.requestPaymentVerification(
            billId
          );

          return true;

        } catch (err) {
          console.error(err);

          setError(
            "Failed to submit payment request."
          );

          return false;

        } finally {
          setSubmitting(false);
        }
      },
      []
    );

  return {
    payment,

    loading,

    submitting,

    error,

    fetchUpiPayment,

    requestVerification,
  };
}
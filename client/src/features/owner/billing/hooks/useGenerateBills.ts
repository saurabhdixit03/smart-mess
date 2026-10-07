import {
  useRef,
  useState,
} from "react";

import { toast } from "sonner";

import { billingApi } from "../api";

import type {
  GenerateBillRequest,
} from "../types";

export function useGenerateBills(
  onSuccess: () => void | Promise<void>
) {
  const [loading, setLoading] =
    useState(false);

  const submittingRef = useRef(false);

  async function generateBills(
    billingMonth: number,
    billingYear: number,
    filters: Omit<
      GenerateBillRequest,
      "billingMonth" | "billingYear"
    > = {}
  ): Promise<boolean> {
    if (submittingRef.current) {
      return false;
    }

    submittingRef.current = true;
    setLoading(true);

    try {
      let response;

      try {
        response = await billingApi.generateBills({
          ...filters,
          billingMonth,
          billingYear,
        });
      } catch (error) {
        toast.error(
          error instanceof Error
            ? error.message
            : "Failed to generate bills."
        );

        return false;
      }

      toast.success(
        response.message ||
          "Bills generated successfully."
      );

      try {
        await onSuccess();
      } catch {
        toast.warning(
          "Bills were generated, but the billing overview could not be refreshed. Please refresh the page."
        );
      }

      return true;
    } finally {
      submittingRef.current = false;
      setLoading(false);
    }
  }

  return {
    loading,
    generateBills,
  };
}
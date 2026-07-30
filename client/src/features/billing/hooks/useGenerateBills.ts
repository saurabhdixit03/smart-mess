import { useState } from "react";
import { toast } from "sonner";

import { billingApi } from "../api";

export function useGenerateBills(
  onSuccess: () => void
) {
  const [loading, setLoading] =
    useState(false);

  const generateBills = async (
    billingMonth: number,
    billingYear: number
  ) => {
    try {
      setLoading(true);

      const response =
        await billingApi.generateBills({
          billingMonth,
          billingYear,
        });

      toast.success(response.message);

      await onSuccess();

    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : "Failed to generate bills."
      );
    } finally {
      setLoading(false);
    }
  };

  return {
    loading,
    generateBills,
  };
}
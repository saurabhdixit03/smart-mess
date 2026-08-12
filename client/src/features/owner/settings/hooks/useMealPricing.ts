import { useCallback, useEffect, useState } from "react";

import { toast } from "sonner";

import { mealPricingApi } from "../api";

import type {
  MealPricingResponse,
  UpdateMealPricingRequest,
} from "../types";

export function useMealPricing() {
  const [pricing, setPricing] =
    useState<MealPricingResponse | null>(
      null
    );

  const [loading, setLoading] =
    useState(true);

  const [saving, setSaving] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const fetchPricing =
    useCallback(async () => {
      try {
        setLoading(true);
        setError(null);

        const response =
          await mealPricingApi.getCurrentPricing();

        setPricing(response.data);
      } catch (err) {
        console.error(err);

        setError(
          "Failed to load meal pricing."
        );
      } finally {
        setLoading(false);
      }
    }, []);

  useEffect(() => {
    fetchPricing();
  }, [fetchPricing]);

  async function updatePricing(
    request: UpdateMealPricingRequest
  ) {
    try {
      setSaving(true);

      const response =
        await mealPricingApi.updatePricing(
          request
        );

      setPricing(response.data);

      toast.success(
        "Meal pricing updated successfully."
      );

      return true;
    } catch (err) {
      console.error(err);

      toast.error(
        "Failed to update meal pricing."
      );

      return false;
    } finally {
      setSaving(false);
    }
  }

  return {
    pricing,
    loading,
    saving,
    error,
    fetchPricing,
    updatePricing,
  };
}
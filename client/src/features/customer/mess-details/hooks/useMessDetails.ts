import { useCallback, useEffect, useState } from "react";

import { messDetailsApi } from "../api/messDetails.api";

import type {
  MealPricingResponse,
  MessSettingsResponse,
} from "../types/messDetails.types";

export function useMessDetails() {
  const [settings, setSettings] =
    useState<MessSettingsResponse | null>(null);

  const [pricing, setPricing] =
    useState<MealPricingResponse | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const fetchMessDetails =
    useCallback(async () => {
      try {
        setLoading(true);
        setError(null);

        const [
          settingsResponse,
          pricingResponse,
        ] = await Promise.all([
          messDetailsApi.getSettings(),
          messDetailsApi.getMealPricing(),
        ]);

        setSettings(
          settingsResponse.data
        );

        setPricing(
          pricingResponse.data
        );
      } catch (err) {
        console.error(err);

        setError(
          "Failed to load mess details."
        );
      } finally {
        setLoading(false);
      }
    }, []);

  useEffect(() => {
    fetchMessDetails();
  }, [fetchMessDetails]);

  return {
    settings,
    pricing,
    loading,
    error,
    refreshMessDetails:
      fetchMessDetails,
  };
}
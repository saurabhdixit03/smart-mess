import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

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

  const requestVersionRef = useRef(0);

  const loadMessDetails = useCallback(
    (): Promise<void> => {
      const requestVersion =
        ++requestVersionRef.current;

      return Promise.all([
        messDetailsApi.getSettings(),
        messDetailsApi.getMealPricing(),
      ])
        .then(([settingsResponse, pricingResponse]) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setSettings(settingsResponse.data);
          setPricing(pricingResponse.data);
          setError(null);
        })
        .catch(() => {
          if (
            requestVersion === requestVersionRef.current
          ) {
            setError("Failed to load mess details.");
          }
        })
        .finally(() => {
          if (
            requestVersion === requestVersionRef.current
          ) {
            setLoading(false);
          }
        });
    },
    []
  );

  const fetchMessDetails = useCallback(
    (): Promise<void> => {
      setLoading(true);
      setError(null);

      return loadMessDetails();
    },
    [loadMessDetails]
  );

  useEffect(() => {
    void loadMessDetails();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadMessDetails]);

  return {
    settings,
    pricing,
    loading,
    error,
    refreshMessDetails: fetchMessDetails,
  };
}
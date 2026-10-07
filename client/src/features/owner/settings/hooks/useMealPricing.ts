import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { toast } from "sonner";

import { mealPricingApi } from "../api";

import type {
  MealPricingResponse,
  UpdateMealPricingRequest,
} from "../types";

function getErrorMessage(
  error: unknown,
  fallback: string
): string {
  return error instanceof Error
    ? error.message
    : fallback;
}

export function useMealPricing() {
  const [pricing, setPricing] =
    useState<MealPricingResponse | null>(null);

  const [scheduledPricing, setScheduledPricing] =
    useState<MealPricingResponse[]>([]);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const mountedRef = useRef(false);
  const requestVersionRef = useRef(0);
  const savingRef = useRef(false);

  const loadPricing = useCallback(
    (): Promise<boolean> => {
      const requestVersion =
        ++requestVersionRef.current;

      return Promise.all([
        mealPricingApi.getCurrentPricing(),
        mealPricingApi.getScheduledPricing(),
      ])
        .then(([currentResponse, scheduledResponse]) => {
          if (
            !mountedRef.current ||
            requestVersion !== requestVersionRef.current
          ) {
            return false;
          }

          setPricing(currentResponse.data);
          setScheduledPricing(scheduledResponse.data);
          setError(null);

          return true;
        })
        .catch((err: unknown) => {
          if (
            mountedRef.current &&
            requestVersion === requestVersionRef.current
          ) {
            setError(
              getErrorMessage(
                err,
                "Failed to load meal pricing."
              )
            );
          }

          return false;
        })
        .finally(() => {
          if (
            mountedRef.current &&
            requestVersion === requestVersionRef.current
          ) {
            setLoading(false);
          }
        });
    },
    []
  );

  const fetchPricing = useCallback(
    (): Promise<boolean> => {
      if (mountedRef.current) {
        setLoading(true);
        setError(null);
      }

      return loadPricing();
    },
    [loadPricing]
  );

  useEffect(() => {
    mountedRef.current = true;

    void loadPricing();

    return () => {
      mountedRef.current = false;
      requestVersionRef.current += 1;
    };
  }, [loadPricing]);

  const updatePricing = useCallback(
    async (
      request: UpdateMealPricingRequest
    ): Promise<boolean> => {
      if (!mountedRef.current || savingRef.current) {
        return false;
      }

      savingRef.current = true;
      setSaving(true);

      try {
        const response =
          await mealPricingApi.updatePricing(request);

        if (!mountedRef.current) {
          return true;
        }

        toast.success(
          response.message ||
            "Meal pricing saved successfully."
        );

        const refreshed = await fetchPricing();

        if (mountedRef.current && !refreshed) {
          toast.warning(
            "Pricing was saved, but the latest prices could not be loaded. Please refresh."
          );
        }

        return true;
      } catch (err: unknown) {
        if (mountedRef.current) {
          toast.error(
            getErrorMessage(
              err,
              "Failed to save meal pricing."
            )
          );
        }

        return false;
      } finally {
        savingRef.current = false;

        if (mountedRef.current) {
          setSaving(false);
        }
      }
    },
    [fetchPricing]
  );

  const cancelScheduledPricing = useCallback(
    async (mealPricingId: number): Promise<boolean> => {
      if (!mountedRef.current || savingRef.current) {
        return false;
      }

      if (
        !Number.isSafeInteger(mealPricingId) ||
        mealPricingId <= 0
      ) {
        toast.error("Invalid meal pricing ID.");
        return false;
      }

      savingRef.current = true;
      setSaving(true);

      try {
        const response =
          await mealPricingApi.cancelScheduledPricing(
            mealPricingId
          );

        if (!mountedRef.current) {
          return true;
        }

        toast.success(
          response.message ||
            "Upcoming meal price change cancelled."
        );

        const refreshed = await fetchPricing();

        if (mountedRef.current && !refreshed) {
          toast.warning(
            "The price change was cancelled, but the latest prices could not be loaded. Please refresh."
          );
        }

        return true;
      } catch (err: unknown) {
        if (mountedRef.current) {
          toast.error(
            getErrorMessage(
              err,
              "Failed to cancel the upcoming price change."
            )
          );
        }

        return false;
      } finally {
        savingRef.current = false;

        if (mountedRef.current) {
          setSaving(false);
        }
      }
    },
    [fetchPricing]
  );

  return {
    pricing,
    scheduledPricing,
    loading,
    saving,
    error,
    fetchPricing,
    updatePricing,
    cancelScheduledPricing,
  };
}
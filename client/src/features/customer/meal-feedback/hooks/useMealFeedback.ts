import {
  useCallback,
  useEffect,
  useState,
} from "react";

import { mealFeedbackApi } from "../api/mealFeedback.api";

import type {
  PendingMealFeedback,
  SubmitMealFeedbackRequest,
} from "../types/mealFeedback.types";

export function useMealFeedback() {
  const [pendingFeedback, setPendingFeedback] =
    useState<PendingMealFeedback | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [submitting, setSubmitting] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const fetchPendingFeedback =
    useCallback(async () => {
      setLoading(true);
      setError(null);

      try {
        const response =
          await mealFeedbackApi.getPendingFeedback();

        setPendingFeedback(response);
      } catch (err: unknown) {
        setPendingFeedback(null);

        setError(
          err instanceof Error
            ? err.message
            : "Failed to load meal feedback."
        );
      } finally {
        setLoading(false);
      }
    }, []);

  const submitFeedback = useCallback(
    async (
      payload: SubmitMealFeedbackRequest
    ) => {
      setSubmitting(true);
      setError(null);

      try {
        await mealFeedbackApi.submitFeedback(
          payload
        );

        setPendingFeedback(null);
      } catch (err: unknown) {
        setError(
          err instanceof Error
            ? err.message
            : "Failed to submit meal feedback."
        );

        throw err;
      } finally {
        setSubmitting(false);
      }
    },
    []
  );

  useEffect(() => {
    void fetchPendingFeedback();
  }, [fetchPendingFeedback]);

  return {
    pendingFeedback,
    loading,
    submitting,
    error,
    fetchPendingFeedback,
    submitFeedback,
  };
}
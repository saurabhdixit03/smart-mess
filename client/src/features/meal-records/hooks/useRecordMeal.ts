import { useState } from "react";

import { createMealRecord } from "../api";

import type {
  CreateMealRecordRequest,
} from "../types";

export function useRecordMeal() {

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  async function recordMeal(
    payload: CreateMealRecordRequest
  ) {

    try {

      setLoading(true);
      setError(null);

      const response =
        await createMealRecord(payload);

      return response;

    } catch (err) {

      setError(
        err instanceof Error
          ? err.message
          : "Failed to record meal."
      );

      return null;

    } finally {

      setLoading(false);

    }

  }

  return {

    recordMeal,

    loading,

    error,

  };

}
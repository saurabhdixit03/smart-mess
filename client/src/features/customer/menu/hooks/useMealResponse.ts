import { useState } from "react";

import { mealResponseApi } from "../api";

import type {
  MealResponse,
  SubmitMealResponseRequest,
} from "../types";

export function useMealResponse() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [response, setResponse] =
    useState<MealResponse | null>(null);

  const submitMealResponse = async (
    payload: SubmitMealResponseRequest
  ) => {
    try {
      setLoading(true);
      setError(null);

      const apiResponse =
        await mealResponseApi.submitMealResponse(
          payload
        );

      setResponse(apiResponse.data);

      return apiResponse.data;

    } catch (err) {

      const message =
        err instanceof Error
          ? err.message
          : "Failed to submit meal response.";

      setError(message);

      throw err;

    } finally {

      setLoading(false);

    }
  };

  return {
    response,
    loading,
    error,
    submitMealResponse,
  };
}
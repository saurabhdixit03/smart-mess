import api, {
  ApiError,
} from "@/lib/api";

import {
  getAccessToken,
} from "@/features/auth/utils/auth.utils";

import type {
  PendingMealFeedback,
  SubmitMealFeedbackRequest,
} from "../types/mealFeedback.types";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL;

const MEAL_FEEDBACK_API_ENDPOINT =
  "/meal-feedback";

export const mealFeedbackApi = {
  getPendingFeedback() {
    return api.get<PendingMealFeedback | null>(
      `${MEAL_FEEDBACK_API_ENDPOINT}/pending`
    );
  },

  async submitFeedback(
    payload: SubmitMealFeedbackRequest
  ): Promise<void> {
    const token = getAccessToken();

    const response = await fetch(
      `${API_BASE_URL}${MEAL_FEEDBACK_API_ENDPOINT}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...(token
            ? {
                Authorization: `Bearer ${token}`,
              }
            : {}),
        },
        body: JSON.stringify(payload),
      }
    );

    if (response.ok) {
      return;
    }

    let message =
      "Failed to submit meal feedback.";

    try {
      const data = await response.json();

      if (
        data &&
        typeof data.message === "string"
      ) {
        message = data.message;
      }
    } catch {
      // Keep the fallback message when the
      // error response has no JSON body.
    }

    throw new ApiError(
      message,
      response.status
    );
  },
};
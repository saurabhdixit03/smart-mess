import api, { ApiError } from "@/lib/api";

import type {
  ApiResponse,
  MealPricingResponse,
  UpdateMealPricingRequest,
} from "../types";

const MEAL_PRICING_ENDPOINT = "/meal-pricing";

export const mealPricingApi = {
  async getCurrentPricing(): Promise<
    ApiResponse<MealPricingResponse | null>
  > {
    try {
      return await api.get<ApiResponse<MealPricingResponse>>(
        MEAL_PRICING_ENDPOINT
      );
    } catch (error: unknown) {
      if (
        error instanceof ApiError &&
        error.status === 404 &&
        error.message === "Meal pricing not configured."
      ) {
        return {
          timestamp: new Date().toISOString(),
          success: true,
          message: "Meal pricing not configured.",
          path: MEAL_PRICING_ENDPOINT,
          data: null,
        };
      }

      throw error;
    }
  },

  getScheduledPricing() {
    return api.get<ApiResponse<MealPricingResponse[]>>(
      `${MEAL_PRICING_ENDPOINT}/scheduled`
    );
  },

  updatePricing(request: UpdateMealPricingRequest) {
    return api.put<ApiResponse<MealPricingResponse>>(
      MEAL_PRICING_ENDPOINT,
      request
    );
  },

  cancelScheduledPricing(mealPricingId: number) {
    return api.delete<ApiResponse<null>>(
      `${MEAL_PRICING_ENDPOINT}/scheduled/${mealPricingId}`
    );
  },
};
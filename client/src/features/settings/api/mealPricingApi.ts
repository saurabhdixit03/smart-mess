import api from "@/lib/api";

import type {
  ApiResponse,
  MealPricingResponse,
  UpdateMealPricingRequest,
} from "../types";

const MEAL_PRICING_ENDPOINT =
  "/meal-pricing";

export const mealPricingApi = {
  getCurrentPricing() {
    return api.get<
      ApiResponse<MealPricingResponse>
    >(MEAL_PRICING_ENDPOINT);
  },

  updatePricing(
    request: UpdateMealPricingRequest
  ) {
    return api.put<
      ApiResponse<MealPricingResponse>
    >(
      MEAL_PRICING_ENDPOINT,
      request
    );
  },
};
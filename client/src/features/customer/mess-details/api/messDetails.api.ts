import api from "@/lib/api";

import type {
  ApiResponse,
  MealPricingResponse,
  MessSettingsResponse,
} from "../types/messDetails.types";

const SETTINGS_ENDPOINT = "/settings";
const MEAL_PRICING_ENDPOINT = "/meal-pricing";

export const messDetailsApi = {
  getSettings() {
    return api.get<
      ApiResponse<MessSettingsResponse>
    >(SETTINGS_ENDPOINT);
  },

  getMealPricing() {
    return api.get<
      ApiResponse<MealPricingResponse>
    >(MEAL_PRICING_ENDPOINT);
  },
};
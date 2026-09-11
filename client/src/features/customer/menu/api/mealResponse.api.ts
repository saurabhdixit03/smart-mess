import api from "@/lib/api";

import { MEAL_RESPONSE_API_ENDPOINT } from "../constants";

import type {
  ApiResponse,
  MealResponse,
  MealResponseAvailability,
  SubmitMealResponseRequest,
} from "../types";

export const mealResponseApi = {
  submitMealResponse(
    payload: SubmitMealResponseRequest
  ) {
    return api.post<ApiResponse<MealResponse>>(
      MEAL_RESPONSE_API_ENDPOINT,
      payload
    );
  },

  getCustomerMealResponse(
    customerId: number,
    menuId: number
  ) {
    return api.get<ApiResponse<MealResponse | null>>(
      `${MEAL_RESPONSE_API_ENDPOINT}/customer/${customerId}/menu/${menuId}`
    );
  },

  getResponseAvailability(
    menuId: number
  ) {
    return api.get<ApiResponse<MealResponseAvailability>>(
      `${MEAL_RESPONSE_API_ENDPOINT}/menu/${menuId}/availability`
    );
  },
};
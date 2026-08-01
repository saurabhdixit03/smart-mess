import api from "@/lib/api";

import { MEAL_RESPONSE_API_ENDPOINT } from "../constants";

import type {
  ApiResponse,
  MealResponse,
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
};
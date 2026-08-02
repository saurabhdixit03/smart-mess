import api from "@/lib/api";

import type {
  ApiResponse,
  MealRecord,
} from "../types";

const MEAL_RECORD_API_ENDPOINT =
  "/meal-records";

export const mealRecordApi = {

  getCustomerMealHistory(
    customerId: number
  ) {
    return api.get<
      ApiResponse<MealRecord[]>
    >(
      `${MEAL_RECORD_API_ENDPOINT}/customer/${customerId}`
    );
  },

};
import api from "@/lib/api";

import {
  DASHBOARD_API_ENDPOINT,
  MEAL_RESPONSE_API_ENDPOINT,
} from "../constants/dashboard.constants";

import type {
  ApiResponse,
  DashboardSummary,
  MealResponse,
  MealSession,
  SubmitMealResponseRequest,
} from "../types/dashboard.types";

export const dashboardApi = {

  getDashboardSummary(
    mealSession: MealSession
  ) {
    return api.get<ApiResponse<DashboardSummary>>(
      `${DASHBOARD_API_ENDPOINT}?mealSession=${mealSession}`
    );
  },

  submitMealResponse(
    payload: SubmitMealResponseRequest
  ) {
    return api.post<ApiResponse<MealResponse>>(
      MEAL_RESPONSE_API_ENDPOINT,
      payload
    );
  },

  getResponsesByMenu(
    menuId: number
  ) {
    return api.get<ApiResponse<MealResponse[]>>(
      `${MEAL_RESPONSE_API_ENDPOINT}/menu/${menuId}`
    );
  },

};
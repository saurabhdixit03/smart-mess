import api from "@/lib/api";

import { INSIGHTS_API_ENDPOINT } from "../constants";

import type {
  ApiResponse,
  MonthlyInsightsResponse,
} from "../types";

export const insightsApi = {

  getMonthlyInsights(
    month: number,
    year: number
  ) {
    return api.get<ApiResponse<MonthlyInsightsResponse>>(
      `${INSIGHTS_API_ENDPOINT}/monthly?month=${month}&year=${year}`
    );
  },

};
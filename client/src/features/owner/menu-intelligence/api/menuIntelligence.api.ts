
import api from "@/lib/api";
import { MENU_INTELLIGENCE_API_ENDPOINT } from "../constants";

import type {
  DishIntelligence,
  DishRatingAnalytics,
  DishFeedbackAnalytics,
  MenuIntelligenceOverview,
} from "../types";

export const menuIntelligenceApi = {
  getDishes() {
    return api.get<DishIntelligence[]>(
      `${MENU_INTELLIGENCE_API_ENDPOINT}/dishes`
    );
  },

  getDishRatings() {
    return api.get<DishRatingAnalytics[]>(
      `${MENU_INTELLIGENCE_API_ENDPOINT}/dish-ratings`
    );
  },

  getDishFeedback() {
    return api.get<DishFeedbackAnalytics[]>(
      `${MENU_INTELLIGENCE_API_ENDPOINT}/dish-feedback`
    );
  },

  getOverview() {
    return api.get<MenuIntelligenceOverview>(
      `${MENU_INTELLIGENCE_API_ENDPOINT}/overview`
    );
  },
};

import api from "@/lib/api";

import type {
  ApiResponse,
  DishCategory,
  DishResponse,
} from "../types/dish.types";

const DISH_API_ENDPOINT = "/dishes";

export const dishApi = {
  searchDishes(
    category: DishCategory,
    query?: string
  ) {
    const params = new URLSearchParams({
      category,
    });

    if (query?.trim()) {
      params.set("query", query.trim());
    }

    return api.get<ApiResponse<DishResponse[]>>(
      `${DISH_API_ENDPOINT}/search?${params.toString()}`
    );
  },
};

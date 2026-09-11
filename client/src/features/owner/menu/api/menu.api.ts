import api from "@/lib/api";

import { MENU_API_ENDPOINT } from "../constants/menu.constants";

import type {
  ApiResponse,
  CreateMenuRequest,
  MenuAvailabilityResponse,
  MenuResponse,
} from "../types/menu.types";

export const menuApi = {
  publishMenu(payload: CreateMenuRequest) {
    return api.post<ApiResponse<MenuResponse>>(
      MENU_API_ENDPOINT,
      payload
    );
  },

  getTodayMenus() {
    return api.get<ApiResponse<MenuResponse[]>>(
      `${MENU_API_ENDPOINT}/today`
    );
  },

  getTodayMenuAvailability() {
    return api.get<ApiResponse<MenuAvailabilityResponse[]>>(
      `${MENU_API_ENDPOINT}/today/availability`
    );
  },

  getMenuHistory() {
    return api.get<ApiResponse<MenuResponse[]>>(
      `${MENU_API_ENDPOINT}/history`
    );
  },

  getMenuById(menuId: number) {
    return api.get<ApiResponse<MenuResponse>>(
      `${MENU_API_ENDPOINT}/${menuId}`
    );
  },
};
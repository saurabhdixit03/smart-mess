import api from "@/lib/api";

import { MENU_API_ENDPOINT } from "../constants";
import type { ApiResponse, Menu } from "../types";

export const menuApi = {
  getTodayMenus() {
    return api.get<ApiResponse<Menu[]>>(
      `${MENU_API_ENDPOINT}/today`
    );
  },
};
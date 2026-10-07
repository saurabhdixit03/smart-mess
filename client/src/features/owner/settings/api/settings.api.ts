import api from "@/lib/api";

import { SETTINGS_API_ENDPOINT } from "../constants";

import type {
  ApiResponse,
  MessSettingsResponse,
  UpdateResponseWindowRequest,
  UpdateWeeklyScheduleRequest,
} from "../types";

export const settingsApi = {
  getSettings() {
    return api.get<ApiResponse<MessSettingsResponse>>(
      SETTINGS_API_ENDPOINT
    );
  },

  updateResponseWindow(
    payload: UpdateResponseWindowRequest
  ) {
    return api.put<ApiResponse<MessSettingsResponse>>(
      `${SETTINGS_API_ENDPOINT}/response-window`,
      payload
    );
  },

  updateWeeklySchedule(
    payload: UpdateWeeklyScheduleRequest
  ) {
    return api.put<ApiResponse<MessSettingsResponse>>(
      `${SETTINGS_API_ENDPOINT}/weekly-schedule`,
      payload
    );
  },
};
import api from "@/lib/api";

import { SETTINGS_API_ENDPOINT } from "../constants";

import type {
  ApiResponse,
  CreateMessSettingsRequest,
  MessSettingsResponse,
  UpdatePaymentSettingsRequest,
  UpdateResponseWindowRequest,
  UpdateWeeklyScheduleRequest,
} from "../types";

export const settingsApi = {
  getSettings() {
    return api.get<
      ApiResponse<MessSettingsResponse>
    >(
      SETTINGS_API_ENDPOINT
    );
  },

  createSettings(
    payload: CreateMessSettingsRequest
  ) {
    return api.post<
      ApiResponse<MessSettingsResponse>
    >(
      SETTINGS_API_ENDPOINT,
      payload
    );
  },

  updatePaymentSettings(
    payload: UpdatePaymentSettingsRequest
  ) {
    return api.put<
      ApiResponse<MessSettingsResponse>
    >(
      `${SETTINGS_API_ENDPOINT}/payment`,
      payload
    );
  },

  updateResponseWindow(
    payload: UpdateResponseWindowRequest
  ) {
    return api.put<
      ApiResponse<MessSettingsResponse>
    >(
      `${SETTINGS_API_ENDPOINT}/response-window`,
      payload
    );
  },

  updateWeeklySchedule(
    payload: UpdateWeeklyScheduleRequest
  ) {
    return api.put<
      ApiResponse<MessSettingsResponse>
    >(
      `${SETTINGS_API_ENDPOINT}/weekly-schedule`,
      payload
    );
  },
};
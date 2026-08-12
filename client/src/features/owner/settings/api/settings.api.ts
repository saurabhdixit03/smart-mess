import api from "@/lib/api";

import { SETTINGS_API_ENDPOINT } from "../constants";

import type {
  ApiResponse,
  CreateMessSettingsRequest,
  MessSettingsResponse,
  UpdateMessSettingsRequest,
} from "../types";

export const settingsApi = {

  getSettings() {
    return api.get<ApiResponse<MessSettingsResponse>>(
      SETTINGS_API_ENDPOINT
    );
  },

  createSettings(
    payload: CreateMessSettingsRequest
  ) {
    return api.post<ApiResponse<MessSettingsResponse>>(
      SETTINGS_API_ENDPOINT,
      payload
    );
  },

  updateSettings(
    payload: UpdateMessSettingsRequest
  ) {
    return api.put<ApiResponse<MessSettingsResponse>>(
      SETTINGS_API_ENDPOINT,
      payload
    );
  },

};
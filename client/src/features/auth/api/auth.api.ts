import api from "@/lib/api";

import type {
  ApiResponse,
  OwnerLoginRequest,
  OwnerLoginResponse,
  OwnerRegistrationRequest,
} from "../types/auth.types";

const OWNER_LOGIN_ENDPOINT = "/auth/owner/login";
const OWNER_REGISTER_ENDPOINT = "/auth/owner/register";

export const authApi = {
  login(payload: OwnerLoginRequest) {
    return api.post<ApiResponse<OwnerLoginResponse>>(
      OWNER_LOGIN_ENDPOINT,
      payload
    );
  },

  register(payload: OwnerRegistrationRequest) {
    return api.post<ApiResponse<OwnerLoginResponse>>(
      OWNER_REGISTER_ENDPOINT,
      payload
    );
  },
};
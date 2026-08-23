import api from "@/lib/api";

import type {
  ApiResponse,
  OwnerLoginRequest,
  OwnerLoginResponse,
  OwnerRegistrationRequest,
  CustomerLoginRequest,
  CustomerLoginResponse,
  CustomerRegistrationRequest,
  ForgotPasswordRequest,
ResetPasswordRequest,
} from "../types/auth.types";

const OWNER_LOGIN_ENDPOINT = "/auth/owner/login";
const OWNER_REGISTER_ENDPOINT = "/auth/owner/register";

const CUSTOMER_LOGIN_ENDPOINT = "/auth/customer/login";
const CUSTOMER_REGISTER_ENDPOINT = "/auth/customer/register";

const OWNER_FORGOT_PASSWORD_ENDPOINT = "/auth/owner/forgot-password";
const CUSTOMER_FORGOT_PASSWORD_ENDPOINT = "/auth/customer/forgot-password";

const RESET_PASSWORD_ENDPOINT = "/auth/reset-password";

export const authApi = {
  // -------------------------
  // Owner Authentication
  // -------------------------

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

  // -------------------------
  // Customer Authentication
  // -------------------------

  customerLogin(payload: CustomerLoginRequest) {
    return api.post<ApiResponse<CustomerLoginResponse>>(
      CUSTOMER_LOGIN_ENDPOINT,
      payload
    );
  },

  customerRegister(payload: CustomerRegistrationRequest) {
    return api.post<ApiResponse<CustomerLoginResponse>>(
      CUSTOMER_REGISTER_ENDPOINT,
      payload
    );
  },

  ownerForgotPassword(
  payload: ForgotPasswordRequest
) {
  return api.post<ApiResponse<void>>(
    OWNER_FORGOT_PASSWORD_ENDPOINT,
    payload
  );
},

customerForgotPassword(
  payload: ForgotPasswordRequest
) {
  return api.post<ApiResponse<void>>(
    CUSTOMER_FORGOT_PASSWORD_ENDPOINT,
    payload
  );
},

resetPassword(
  payload: ResetPasswordRequest
) {
  return api.post<ApiResponse<void>>(
    RESET_PASSWORD_ENDPOINT,
    payload
  );
},
};
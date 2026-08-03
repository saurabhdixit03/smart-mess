import api from "@/lib/api";

import { CUSTOMER_API_ENDPOINT } from "../constants";

import type {
  ApiResponse,
  CustomerProfile,
  UpdateProfileRequest,
} from "../types";

export const profileApi = {
  getProfile(
    customerId: number
  ) {
    return api.get<
      ApiResponse<CustomerProfile>
    >(
      `${CUSTOMER_API_ENDPOINT}/${customerId}`
    );
  },

  updateProfile(
    customerId: number,
    request: UpdateProfileRequest
  ) {
    return api.put<
      ApiResponse<CustomerProfile>
    >(
      `${CUSTOMER_API_ENDPOINT}/${customerId}`,
      {
        ...request,

        // Backend currently requires remarks.
        remarks: "",
      }
    );
  },
};
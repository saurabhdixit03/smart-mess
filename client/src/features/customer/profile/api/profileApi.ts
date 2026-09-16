import api from "@/lib/api";

import { CUSTOMER_API_ENDPOINT } from "../constants";

import type {
  ApiResponse,
  CustomerProfile,
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
};
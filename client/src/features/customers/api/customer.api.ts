import api from "@/lib/api";
import { CUSTOMER_API_ENDPOINT } from "../constants/customer.constants";

import type {
  ApiResponse,
  CreateCustomerRequest,
  CustomerResponse,
  UpdateCustomerRequest,
} from "../types/customer.types";

CUSTOMER_API_ENDPOINT

export const customerApi = {
  getAllCustomers() {
    return api.get<ApiResponse<CustomerResponse[]>>(CUSTOMER_API_ENDPOINT);
  },

  getCustomerById(customerId: number) {
    return api.get<ApiResponse<CustomerResponse>>(
      `${CUSTOMER_API_ENDPOINT}/${customerId}`
    );
  },

  createCustomer(payload: CreateCustomerRequest) {
    return api.post<ApiResponse<CustomerResponse>>(
      CUSTOMER_API_ENDPOINT,
      payload
    );
  },

  updateCustomer(
    customerId: number,
    payload: UpdateCustomerRequest
  ) {
    return api.put<ApiResponse<CustomerResponse>>(
      `${CUSTOMER_API_ENDPOINT}/${customerId}`,
      payload
    );
  },

  deleteCustomer(customerId: number) {
    return api.delete<ApiResponse<void>>(
      `${CUSTOMER_API_ENDPOINT}/${customerId}`
    );
  },
};
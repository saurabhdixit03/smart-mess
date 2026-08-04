import api from "@/lib/api";

import { PAYMENT_API_ENDPOINT } from "../constants";

import type {
  ApiResponse,
  UpiPayment,
} from "../types";

export const paymentApi = {
  generateUpiPayment(
    billId: number
  ) {
    return api.get<ApiResponse<UpiPayment>>(
      `${PAYMENT_API_ENDPOINT}/upi/${billId}`
    );
  },

  requestPaymentVerification(
    billId: number
  ) {
    return api.post<ApiResponse<void>>(
      `${PAYMENT_API_ENDPOINT}/request/${billId}`,
      {}
    );
  },
};
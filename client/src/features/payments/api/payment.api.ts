import api from "@/lib/api";

import { PAYMENT_API_ENDPOINT } from "../constants";

import type {
  ApiResponse,
  CreatePaymentRequest,
  PaymentOverviewResponse,
  PaymentResponse,
  UpiPaymentResponse,
} from "../types";

export const paymentApi = {

  getPaymentOverview() {
    return api.get<ApiResponse<PaymentOverviewResponse>>(
      `${PAYMENT_API_ENDPOINT}/overview`
    );
  },

  collectPayment(
    request: CreatePaymentRequest
  ) {
    return api.post<ApiResponse<PaymentResponse>>(
      PAYMENT_API_ENDPOINT,
      request
    );
  },

  requestUpiPayment(
    billId: number
  ) {
    return api.post<ApiResponse<void>>(
      `${PAYMENT_API_ENDPOINT}/request/${billId}`,
      {}
    );
  },

  generateUpiPayment(
    billId: number
  ) {
    return api.get<ApiResponse<UpiPaymentResponse>>(
      `${PAYMENT_API_ENDPOINT}/upi/${billId}`
    );
  },

  getPaymentByBill(
    billId: number
  ) {
    return api.get<ApiResponse<PaymentResponse>>(
      `${PAYMENT_API_ENDPOINT}/bill/${billId}`
    );
  },

};
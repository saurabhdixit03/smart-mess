import api from "@/lib/api";

import {
  BILL_API_ENDPOINT,
} from "../constants";

import type {
  ApiResponse,
  Bill,
  BillDetail,
  UpiPayment,
} from "../types";

const PAYMENT_API_ENDPOINT =
  "/payments";

export const billingApi = {
  getCustomerBills(
    customerId: number
  ) {
    return api.get<ApiResponse<Bill[]>>(
      `${BILL_API_ENDPOINT}/customer/${customerId}`
    );
  },

  getBillDetails(
    billId: number
  ) {
    return api.get<ApiResponse<BillDetail>>(
      `${BILL_API_ENDPOINT}/${billId}`
    );
  },

  getUpiPayment(
    billId: number
  ) {
    return api.get<ApiResponse<UpiPayment>>(
      `${PAYMENT_API_ENDPOINT}/upi/${billId}`
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
};
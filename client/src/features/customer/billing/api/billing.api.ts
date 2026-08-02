import api from "@/lib/api";

import { BILL_API_ENDPOINT } from "../constants";

import type {
  ApiResponse,
  Bill,
  BillDetail,
} from "../types";

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
};
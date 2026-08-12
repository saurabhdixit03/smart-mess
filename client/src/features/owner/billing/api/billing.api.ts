import api from "@/lib/api";

import { BILLING_API_ENDPOINT } from "../constants";

import type {
  ApiResponse,
  BillDetailResponse,
  BillResponse,
  BillingOverviewResponse,
  GenerateBillRequest,
} from "../types";

export const billingApi = {
  getBillingOverview(
    billingMonth: number,
    billingYear: number
  ) {
    return api.get<ApiResponse<BillingOverviewResponse>>(
      `${BILLING_API_ENDPOINT}/overview?billingMonth=${billingMonth}&billingYear=${billingYear}`
    );
  },

  generateBills(payload: GenerateBillRequest) {
    return api.post<ApiResponse<BillResponse[]>>(
      `${BILLING_API_ENDPOINT}/generate`,
      payload
    );
  },

  getBillDetails(
    billId: number
  ) {
    return api.get<ApiResponse<BillDetailResponse>>(
      `${BILLING_API_ENDPOINT}/${billId}`
    );
  },
};
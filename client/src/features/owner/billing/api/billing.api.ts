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
  /**
   * Omit both parameters, or pass null for both,
   * to retrieve all billing periods.
   */
  getBillingOverview(
    billingMonth: number | null = null,
    billingYear: number | null = null
  ) {
    const hasMonth = billingMonth !== null;
    const hasYear = billingYear !== null;

    if (hasMonth !== hasYear) {
      throw new Error(
        "Supply both billing month and year, or omit both."
      );
    }

    const query =
      hasMonth && hasYear
        ? `?billingMonth=${billingMonth}&billingYear=${billingYear}`
        : "";

    return api.get<ApiResponse<BillingOverviewResponse>>(
      `${BILLING_API_ENDPOINT}/overview${query}`
    );
  },

  generateBills(payload: GenerateBillRequest) {
    return api.post<ApiResponse<BillResponse[]>>(
      `${BILLING_API_ENDPOINT}/generate`,
      payload
    );
  },

  getBillDetails(billId: number) {
    return api.get<ApiResponse<BillDetailResponse>>(
      `${BILLING_API_ENDPOINT}/${billId}`
    );
  },
};
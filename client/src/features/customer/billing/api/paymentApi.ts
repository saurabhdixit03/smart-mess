import api from "@/lib/api";

import { PAYMENT_API_ENDPOINT } from "../constants";

import type { PaymentCheckoutResponse } from "../types";

export const paymentApi = {
  createCheckout(billId: number) {
    return api.post<PaymentCheckoutResponse>(
      `${PAYMENT_API_ENDPOINT}/checkout/bill/${billId}`
    );
  },

  verifyCheckout(paymentOrderId: number) {
    return api.post<PaymentCheckoutResponse>(
      `${PAYMENT_API_ENDPOINT}/orders/${paymentOrderId}/verify`
    );
  },
};
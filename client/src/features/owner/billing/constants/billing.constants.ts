/**
 * Billing Module Constants
 * ---------------------------------------
 * All billing-related constants should live here.
 * Avoid hardcoding values anywhere else.
 */

export const BILLING_API_ENDPOINT = "/bills";

export const BILL_STATUS = {
  PAID: "PAID",
  UNPAID: "UNPAID",
  PAYMENT_PENDING: "PAYMENT_PENDING",
} as const;

export const BILLING_MESSAGES = {
  GENERATE_SUCCESS: "Bills generated successfully.",
};

export const BILLING_FORM = {
  DEFAULT_VALUES: {
    billingMonth: new Date().getMonth() + 1,
    billingYear: new Date().getFullYear(),
  },
};
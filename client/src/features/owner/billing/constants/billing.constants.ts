export const BILLING_API_ENDPOINT = "/bills";

export const BILL_STATUS = {
  PAID: "PAID",
  UNPAID: "UNPAID",
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
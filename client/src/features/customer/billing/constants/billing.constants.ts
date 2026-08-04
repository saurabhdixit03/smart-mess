export const BILL_API_ENDPOINT =
  "/bills";

export const PAYMENT_API_ENDPOINT =
  "/payments";

export const BILL_STATUS_LABELS = {
  PAID: "Paid",
  UNPAID: "Unpaid",
  PAYMENT_PENDING: "Payment Pending",
} as const;

export const BILL_STATUS_VARIANTS = {
  PAID: "success",
  UNPAID: "warning",
  PAYMENT_PENDING: "info",
} as const;

export const MONTH_NAMES = [
  "",
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
] as const;
export const BILL_API_ENDPOINT =
  "/bills";

export const BILL_STATUS_LABELS = {
  PAID: "Paid",
  UNPAID: "Unpaid",
} as const;

export const BILL_STATUS_VARIANTS = {
  PAID: "success",
  UNPAID: "warning",
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
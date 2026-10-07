export type BillStatus = "UNPAID" | "PAID";

export interface Bill {
  billId: number;
  customerId: number;
  customerName: string;
  billingMonth: number;
  billingYear: number;
  mealRecordCount: number;
  totalAmount: number;
  billStatus: BillStatus;
  generatedAt: string;
}

export interface BillingSummary {
  totalBills: number;
  paidBills: number;
  unpaidBills: number;
  totalRevenue: number;
  collectedRevenue: number;
  pendingRevenue: number;
}

export interface BillingOverview {
  summary: BillingSummary;
  bills: Bill[];
}

/**
 * Generate bills from unbilled meal records.
 *
 * Omit customerId to include all eligible customers.
 * Dates use YYYY-MM-DD.
 */
export interface GenerateBillRequest {
  billingMonth: number;
  billingYear: number;

  customerId?: number | null;

  startDate?: string | null;
  endDate?: string | null;
}

export type BillResponse = Bill;

export type BillingSummaryResponse = BillingSummary;

export type BillingOverviewResponse = BillingOverview;

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  path: string;
  data: T;
  timestamp: string;
}

export interface MealRecordResponse {
  mealRecordId: number;

  customerId: number;
  customerName: string;

  menuId: number;
  mealResponseId: number | null;

  mealSession: "LUNCH" | "DINNER";
  mealOption: "FULL" | "HALF";

  mealPrice: number;

  extraRotiCount: number;
  extraRotiPrice: number;

  totalAmount: number;
  collectedAt: string;
}

export interface BillPaymentReceiptResponse {
  paymentId: number;
  paymentAmount: number;
  paymentMode: "CASHFREE";
  paidAt: string;

  /**
   * Historical payments may not have gateway metadata.
   * Null must not be treated as a production payment.
   */
  environment: "SANDBOX" | "PRODUCTION" | null;
  currency: string | null;
  gatewayOrderId: string | null;
  gatewayPaymentId: string | null;
}

export interface BillDetailResponse extends BillResponse {
  messId: number;
  messName: string;

  customerMobileNumber: string;
  customerEmail: string;

  mealRecords: MealRecordResponse[];

  payment: BillPaymentReceiptResponse | null;
}
export type BillStatus =
  | "UNPAID"
  | "PAID"
  | "PAYMENT_PENDING";

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

export interface GenerateBillRequest {
  billingMonth: number;
  billingYear: number;
}

export interface BillResponse {
  billId: number;
  customerId: number;
  customerName: string;
  billingMonth: number;
  billingYear: number;
  mealRecordCount: number;
  totalAmount: number;
  billStatus: "UNPAID" | "PAID" | "PAYMENT_PENDING";
  generatedAt: string;
}

export interface BillingSummaryResponse {
  totalBills: number;
  paidBills: number;
  unpaidBills: number;
  totalRevenue: number;
  collectedRevenue: number;
  pendingRevenue: number;
}

export interface BillingOverviewResponse {
  summary: BillingSummaryResponse;
  bills: BillResponse[];
}

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
  mealResponseId: number;

  mealSession: "LUNCH" | "DINNER";

  mealOption: "FULL" | "HALF";

  mealPrice: number;

  extraRotiCount: number;

  extraRotiPrice: number;

  totalAmount: number;

  collectedAt: string;
}

export interface BillDetailResponse
  extends BillResponse {
  mealRecords: MealRecordResponse[];
}
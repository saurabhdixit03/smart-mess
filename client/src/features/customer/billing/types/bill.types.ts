import type {
  BillDocumentData,
} from "@/features/billing/types";

export type BillStatus = "UNPAID" | "PAID";

export type MealSession = "LUNCH" | "DINNER";

export type MealOption = "FULL" | "HALF";

export interface MealRecord {
  mealRecordId: number;
  customerId: number;
  customerName: string;
  menuId: number;
  mealResponseId: number | null;
  mealSession: MealSession;
  mealOption: MealOption;
  mealPrice: number;
  extraRotiCount: number;
  extraRotiPrice: number;
  totalAmount: number;
  collectedAt: string;
}

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

export type BillDetail = BillDocumentData;

export type PaymentOrderStatus =
  | "CREATING"
  | "ACTIVE"
  | "PAID"
  | "EXPIRED"
  | "TERMINATION_REQUESTED"
  | "TERMINATED"
  | "CREATION_FAILED"
  | "RECONCILIATION_REQUIRED";

export type PaymentEnvironment = "SANDBOX" | "PRODUCTION";

export interface PaymentCheckout {
  paymentOrderId: number;
  billId: number;
  gatewayOrderId: string;
  paymentSessionId: string | null;
  amount: number;
  currency: string;
  environment: PaymentEnvironment;
  status: PaymentOrderStatus;
  expiresAt: string | null;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  path: string;
  data: T;
}

export type BillsResponse = ApiResponse<Bill[]>;

export type BillDetailResponse = ApiResponse<BillDetail>;

export type PaymentCheckoutResponse =
  ApiResponse<PaymentCheckout>;
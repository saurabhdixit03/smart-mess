export type BillDocumentStatus = "UNPAID" | "PAID";

export interface BillDocumentMeal {
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

export interface BillDocumentPayment {
  paymentId: number;
  paymentAmount: number;
  paymentMode: "CASHFREE";
  paidAt: string;

  environment: "SANDBOX" | "PRODUCTION" | null;
  currency: string | null;
  gatewayOrderId: string | null;
  gatewayPaymentId: string | null;
}

export interface BillDocumentData {
  billId: number;

  messId: number;
  messName: string;

  customerId: number;
  customerName: string;
  customerMobileNumber: string;
  customerEmail: string;

  billingMonth: number;
  billingYear: number;

  mealRecordCount: number;
  totalAmount: number;
  billStatus: BillDocumentStatus;
  generatedAt: string;

  mealRecords: BillDocumentMeal[];
  payment: BillDocumentPayment | null;
}
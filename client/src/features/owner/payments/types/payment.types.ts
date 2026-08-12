export type PaymentMode =
  | "CASH"
  | "UPI";

export interface CreatePaymentRequest {
  billId: number;
  paymentMode: PaymentMode;
}

export interface PaymentResponse {
  paymentId: number;
  billId: number;
  customerId: number;
  customerName: string;
  paymentAmount: number;
  paymentMode: PaymentMode;
  paidAt: string;
}

export interface PendingPaymentResponse {
  billId: number;
  customerId: number;
  customerName: string;
  billingMonth: number;
  billingYear: number;
  totalAmount: number;
  billStatus: BillResponse["billStatus"];
}

export interface PaymentOverviewResponse {
  unpaidBillCount: number;
  pendingRequestCount: number;
  paidBillCount: number;
  totalCollectedAmount: number;

  unpaidBills: BillResponse[];
  pendingPayments: PendingPaymentResponse[];
}

export interface UpiPaymentResponse {
  upiUrl: string;
  upiId: string;
  receiverName: string;
  amount: number;
  billId: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  path: string;
  data: T;
  timestamp: string;
}

export interface BillResponse {
  billId: number;
  customerId: number;
  customerName: string;
  billingMonth: number;
  billingYear: number;
  mealRecordCount: number;
  totalAmount: number;
  billStatus: "PAID" | "UNPAID" | "PAYMENT_PENDING";
  generatedAt: string;
}

export interface PaymentOverviewResponse {
  unpaidBillCount: number;
  pendingRequestCount: number;
  paidBillCount: number;
  totalCollectedAmount: number;

  unpaidBills: BillResponse[];
  pendingPayments: PendingPaymentResponse[];
}
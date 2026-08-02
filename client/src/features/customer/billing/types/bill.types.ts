export type BillStatus =
  | "PAID"
  | "UNPAID";

export type MealSession =
  | "LUNCH"
  | "DINNER";

export type MealOption =
  | "FULL"
  | "HALF";

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

export interface BillDetail extends Bill {
  mealRecords: MealRecord[];
}

export interface ApiResponse<T> {
  success: boolean;

  message: string;

  path: string;

  data: T;
}

export type BillsResponse =
  ApiResponse<Bill[]>;

export type BillDetailResponse =
  ApiResponse<BillDetail>;
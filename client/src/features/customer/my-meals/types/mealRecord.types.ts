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

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  path: string;
  data: T;
}

export type MealRecordsResponse =
  ApiResponse<MealRecord[]>;
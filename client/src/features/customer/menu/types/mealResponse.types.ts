export type MealResponseStatus =
  | "ACCEPTED"
  | "DECLINED";

export type MealOption =
  | "HALF"
  | "FULL";

export interface SubmitMealResponseRequest {
  customerId: number;
  menuId: number;
  responseStatus: MealResponseStatus;
  mealOption: MealOption | null;
  extraRotiCount: number;
}

export interface MealResponse {
  mealResponseId: number;
  customerId: number;
  customerName: string;
  mobileNumber: string;
  menuId: number;
  responseStatus: MealResponseStatus;
  mealOption: MealOption | null;
  extraRotiCount: number;
  respondedAt: string;
}
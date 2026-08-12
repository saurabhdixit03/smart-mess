export type MealResponseStatus =
  | "ACCEPTED"
  | "DECLINED";

export type MealOption =
  | "HALF"
  | "FULL";

export type MealSession =
  | "LUNCH"
  | "DINNER";

/**
 * Dashboard Customer Queue
 * Mirrors DashboardCustomerResponse.java
 */
export type DashboardCustomer = {

  mealResponseId: number;

  customerId: number;

  customerName: string;

  responseStatus: MealResponseStatus;

  mealOption: MealOption;

  extraRotiCount: number;

  respondedAt: string;

  collected: boolean;

};

/**
 * Temporary.
 * Still used by Meal Response module.
 * Will be removed from Dashboard after refactor.
 */
export type MealResponse = {

  mealResponseId: number;

  customerId: number;

  customerName: string;

  mobileNumber: string;

  menuId: number;

  responseStatus: MealResponseStatus;

  mealOption: MealOption | null;

  extraRotiCount: number;

  respondedAt: string;

};

export type DashboardSummary = {

  menuId: number;

  menuDate: string;

  mealSession: MealSession;

  activeCustomers: number;

  acceptedResponses: number;

  declinedResponses: number;

  pendingResponses: number;

  expectedFullMeals: number;

  expectedHalfMeals: number;

  baseRotisRequired: number;

  expectedExtraRotis: number;

  totalRotisRequired: number;

  collectionQueue: DashboardCustomer[];

recentActivities: DashboardCustomer[];

};

export type SubmitMealResponseRequest = {

  customerId: number;

  menuId: number;

  responseStatus: MealResponseStatus;

  mealOption?: MealOption;

  extraRotiCount?: number;

};

export type ApiResponse<T> = {

  success: boolean;

  message: string;

  path: string;

  timestamp: string;

  data: T;

};
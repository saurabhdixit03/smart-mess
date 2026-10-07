export type MealSession = "LUNCH" | "DINNER";

export type DayOfWeek =
  | "MONDAY"
  | "TUESDAY"
  | "WEDNESDAY"
  | "THURSDAY"
  | "FRIDAY"
  | "SATURDAY"
  | "SUNDAY";

export interface UpdateResponseWindowRequest {
  lunchResponseCutoff: string;
  dinnerResponseCutoff: string;
}

export interface UpdateWeeklyScheduleRequest {
  weeklyClosedDay: DayOfWeek | null;
  weeklyLunchClosed: boolean;
  weeklyDinnerClosed: boolean;
}

export interface MessSettingsResponse {
  settingsId: number;
  lunchResponseCutoff: string | null;
  dinnerResponseCutoff: string | null;
  weeklyClosedDay: DayOfWeek | null;
  weeklyLunchClosed: boolean;
  weeklyDinnerClosed: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateMessClosureRequest {
  startDate: string;
  startSession: MealSession;
  endDate: string;
  endSession: MealSession;
  reason: string;
}

export interface UpdateMessClosureRequest {
  startDate: string;
  startSession: MealSession;
  endDate: string;
  endSession: MealSession;
  reason: string;
}

export interface MessClosureResponse {
  closureId: number;
  startDate: string;
  startSession: MealSession;
  endDate: string;
  endSession: MealSession;
  reason: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApiResponse<T> {
  timestamp: string;
  success: boolean;
  message: string;
  path: string;
  data: T;
}

export interface MealPricingResponse {
  mealPricingId: number;
  halfMealPrice: number;
  fullMealPrice: number;
  extraRotiPrice: number;
  updatedAt: string;
  effectiveFrom: string;
}

export interface UpdateMealPricingRequest {
  halfMealPrice: number;
  fullMealPrice: number;
  extraRotiPrice: number;

  // YYYY-MM-DD for a scheduled change; omit for immediate pricing.
  effectiveDate?: string | null;
}
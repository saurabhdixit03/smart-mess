export type DayOfWeek =
  | "MONDAY"
  | "TUESDAY"
  | "WEDNESDAY"
  | "THURSDAY"
  | "FRIDAY"
  | "SATURDAY"
  | "SUNDAY";

/**
 * Read-only mess operational settings
 * available to customers.
 */
export interface MessSettingsResponse {
  settingsId: number | null;

  upiId: string | null;
  receiverName: string | null;

  lunchResponseCutoff: string | null;
  dinnerResponseCutoff: string | null;

  weeklyClosedDay: DayOfWeek | null;
  weeklyLunchClosed: boolean;
  weeklyDinnerClosed: boolean;

  createdAt: string | null;
  updatedAt: string | null;
}

/**
 * Current meal pricing available
 * to customers.
 */
export interface MealPricingResponse {
  mealPricingId: number;

  halfMealPrice: number;
  fullMealPrice: number;
  extraRotiPrice: number;

  updatedAt: string;
}

export interface ApiResponse<T> {
  timestamp: string;
  success: boolean;
  message: string;
  path: string;
  data: T;
}
export type DayOfWeek =
  | "MONDAY"
  | "TUESDAY"
  | "WEDNESDAY"
  | "THURSDAY"
  | "FRIDAY"
  | "SATURDAY"
  | "SUNDAY";

/**
 * Read-only mess operational settings.
 */
export interface MessSettingsResponse {
  settingsId: number | null;

  lunchResponseCutoff: string | null;
  dinnerResponseCutoff: string | null;

  weeklyClosedDay: DayOfWeek | null;
  weeklyLunchClosed: boolean;
  weeklyDinnerClosed: boolean;

  createdAt: string | null;
  updatedAt: string | null;
}

/**
 * Current effective meal pricing.
 */
export interface MealPricingResponse {
  mealPricingId: number;

  halfMealPrice: number;
  fullMealPrice: number;
  extraRotiPrice: number;

  effectiveFrom: string;
  updatedAt: string;
}

export interface ApiResponse<T> {
  timestamp: string;
  success: boolean;
  message: string;
  path: string;
  data: T;
}
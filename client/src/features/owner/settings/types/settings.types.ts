export type MealSession =
  | "LUNCH"
  | "DINNER";

export type DayOfWeek =
  | "MONDAY"
  | "TUESDAY"
  | "WEDNESDAY"
  | "THURSDAY"
  | "FRIDAY"
  | "SATURDAY"
  | "SUNDAY";

/**
 * Initial mess settings creation.
 *
 * Operational settings are configured
 * separately after creation.
 */
export interface CreateMessSettingsRequest {
  upiId: string;
  receiverName: string;
}

/**
 * Payment settings update.
 */
export interface UpdatePaymentSettingsRequest {
  upiId: string;
  receiverName: string;
}

/**
 * Customer response cutoff settings.
 *
 * Values are sent to Spring as LocalTime,
 * for example "11:30" or "19:30".
 */
export interface UpdateResponseWindowRequest {
  lunchResponseCutoff: string;
  dinnerResponseCutoff: string;
}

/**
 * Recurring weekly closure settings.
 *
 * weeklyClosedDay can be null when
 * no recurring weekly closure is configured.
 */
export interface UpdateWeeklyScheduleRequest {
  weeklyClosedDay: DayOfWeek | null;
  weeklyLunchClosed: boolean;
  weeklyDinnerClosed: boolean;
}

/**
 * Complete mess settings returned
 * by the backend.
 */
export interface MessSettingsResponse {
  settingsId: number;

  upiId: string;
  receiverName: string;

  lunchResponseCutoff: string | null;
  dinnerResponseCutoff: string | null;

  weeklyClosedDay: DayOfWeek | null;
  weeklyLunchClosed: boolean;
  weeklyDinnerClosed: boolean;

  createdAt: string;
  updatedAt: string;
}

/**
 * Temporary mess closure creation.
 */
export interface CreateMessClosureRequest {
  startDate: string;
  startSession: MealSession;

  endDate: string;
  endSession: MealSession;

  reason: string;
}

/**
 * Temporary mess closure update.
 */
export interface UpdateMessClosureRequest {
  startDate: string;
  startSession: MealSession;

  endDate: string;
  endSession: MealSession;

  reason: string;
}

/**
 * Temporary closure returned
 * by the backend.
 */
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

/**
 * Meal pricing.
 */
export interface MealPricingResponse {
  mealPricingId: number;

  halfMealPrice: number;
  fullMealPrice: number;
  extraRotiPrice: number;

  updatedAt: string;
}

export interface UpdateMealPricingRequest {
  halfMealPrice: number;
  fullMealPrice: number;
  extraRotiPrice: number;
}
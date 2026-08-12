export type MealSession = "LUNCH" | "DINNER";

export interface CreateMenuRequest {
  menuDate: string;
  mealSession: MealSession;
  sabjiOne: string;
  sabjiTwo: string;
  dal: string;
  rice: string;
  sweet: string;
}

export interface MenuResponse {
  menuId: number;
  menuDate: string;
  mealSession: MealSession;
  sabjiOne: string;
  sabjiTwo: string;
  dal: string;
  rice: string;
  sweet: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  path: string;
  data: T;
  timestamp: string;
}
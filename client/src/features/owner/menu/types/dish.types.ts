export type DishCategory =
  | "SABJI"
  | "DAL"
  | "RICE"
  | "SWEET";

export interface DishResponse {
  dishId: number;
  name: string;
  category: DishCategory;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  path: string;
  data: T;
  timestamp: string;
}

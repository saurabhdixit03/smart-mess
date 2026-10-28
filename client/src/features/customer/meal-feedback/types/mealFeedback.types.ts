export interface PendingMealFeedback {
  mealRecordId: number;
  mealDate: string;
  mealSession: "LUNCH" | "DINNER";
  dishes: DishFeedbackItem[];
}

export interface DishFeedbackItem {
  dishId: number;
  dishName: string;
}

export interface DishRating {
  dishId: number;
  rating: number;
}

export interface SubmitMealFeedbackRequest {
  mealRecordId: number;
  dishRatings: DishRating[];
  commonComment?: string;
}
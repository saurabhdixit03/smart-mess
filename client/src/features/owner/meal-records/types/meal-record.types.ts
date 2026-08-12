export type MealSession = "LUNCH" | "DINNER";

export type MealOption = "FULL" | "HALF";

export type CollectionQueueItem = {
  customerId: number;

  customerName: string;

  menuId: number;

  mealResponseId: number;

  mealOption: MealOption;

  extraRotiCount: number;
};

export type CollectionQueueResponse = {
  timestamp: string;

  success: boolean;

  message: string;

  path: string;

  data: CollectionQueueItem[];
};

export type CreateMealRecordRequest = {
  customerId: number;

  menuId: number;

  mealResponseId: number | null;

  mealOption: MealOption;

  extraRotiCount: number;
};

export type MealRecordResponse = {
  timestamp: string;

  success: boolean;

  message: string;

  path: string;

  data: {
    mealRecordId: number;
  };
};

// for history table 
export type TodayMealRecord = {
  mealRecordId: number;

  customerId: number;

  customerName: string;

  mealSession: MealSession;

  mealOption: MealOption;

  extraRotiCount: number;

  collectedAt: string;
};

export type TodayMealRecordResponse = {
  timestamp: string;

  success: boolean;

  message: string;

  path: string;

  data: TodayMealRecord[];
};
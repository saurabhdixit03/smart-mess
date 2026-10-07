export type MealSession = "LUNCH" | "DINNER";

export type MealOption = "FULL" | "HALF";

export type MealResponseStatus = "ACCEPTED" | "DECLINED";

/*
 * Customer selected for recording a meal.
 * Walk-in customers may have no meal response.
 */
export type MealCollectionSelection = {
  customerId: number;
  customerName: string;
  menuId: number;
  mealResponseId: number | null;
  mealOption: MealOption;
  extraRotiCount: number;
};

/*
 * Default queue contains only response-linked customers.
 */
export type CollectionQueueItem =
  MealCollectionSelection & {
    mealResponseId: number;
  };

export type CollectionQueueResponse = {
  timestamp: string;
  success: boolean;
  message: string;
  path: string;
  data: CollectionQueueItem[];
};

/*
 * Active customer search result for the selected meal.
 */
export type CollectionCustomer =
  MealCollectionSelection & {
    mobileNumber: string;
    responseStatus: MealResponseStatus | null;
    collected: boolean;
  };

export type CollectionCustomerResponse = {
  timestamp: string;
  success: boolean;
  message: string;
  path: string;
  data: CollectionCustomer[];
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
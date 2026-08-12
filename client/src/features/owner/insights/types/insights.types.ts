export interface FinancialInsightsResponse {
  billsGenerated: number;
  paidBills: number;
  pendingBills: number;

  totalRevenue: number;
  collectedRevenue: number;
  pendingRevenue: number;

  collectionRate: number;
}

export interface CustomerInsightsResponse {
  activeCustomers: number;
}

export interface MealInsightsResponse {
  totalMeals: number;
  fullMeals: number;
  halfMeals: number;

  totalRotis: number;
  extraRotis: number;
}

export interface MonthlyInsightsResponse {
  month: string;
  year: number;

  financial: FinancialInsightsResponse;

  customers: CustomerInsightsResponse;

  meals: MealInsightsResponse;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  path: string;
  data: T;
  timestamp: string;
}
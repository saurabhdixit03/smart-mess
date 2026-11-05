
export interface DishIntelligence {
  dishId: number;
  dishName: string;
  active: boolean;
  totalRatings: number;
  averageRating: number | null;
  oneStarCount: number;
  twoStarCount: number;
  threeStarCount: number;
  fourStarCount: number;
  fiveStarCount: number;
  totalComments: number;
  aiSuggestion: DishAiSuggestion | null;
}

export interface DishAiSuggestion {
  observation: string;
  recommendation: string;
  priority: SuggestionPriority;
}

export type SuggestionPriority = "HIGH" | "MEDIUM" | "LOW";

export interface MenuIntelligenceSuggestion {
  dishName: string | null;
  observation: string;
  recommendation: string;
  priority: SuggestionPriority;
}

export interface MenuIntelligenceAnalysis {
  periodStart: string;
  periodEnd: string;
  completedAt: string;
  suggestionCount: number;
}

export interface MenuIntelligenceOverview {
  analysis: MenuIntelligenceAnalysis | null;
  suggestions: MenuIntelligenceSuggestion[];
}

// Legacy analytics types retained for backward compatibility.
export interface DishRatingAnalytics {
  dishName: string;
  totalRatings: number;
  averageRating: number;
  oneStarCount: number;
  twoStarCount: number;
  threeStarCount: number;
  fourStarCount: number;
  fiveStarCount: number;
}

export interface DishFeedbackAnalytics {
  dishName: string;
  totalComments: number;
}

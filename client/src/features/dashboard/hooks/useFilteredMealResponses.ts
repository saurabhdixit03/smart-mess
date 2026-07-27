import { useMemo } from "react";

import type { MealResponse } from "../types/dashboard.types";

export function useFilteredMealResponses(
  mealResponses: MealResponse[],
  searchTerm: string
) {
  const filteredResponses = useMemo(() => {
    const query = searchTerm
      .trim()
      .toLowerCase();

    if (!query) {
      return mealResponses;
    }

    return mealResponses.filter(
      (response) =>
        response.customerName
          .toLowerCase()
          .includes(query) ||
        response.mobileNumber.includes(
          query
        )
    );
  }, [mealResponses, searchTerm]);

  return {
    filteredResponses,
  };
}
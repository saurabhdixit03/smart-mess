export const MEAL_RESPONSE_API_ENDPOINT = "/meal-responses";

export const MEAL_RESPONSE_STATUS = {
  ACCEPTED: "ACCEPTED",
  DECLINED: "DECLINED",
} as const;

export const MEAL_OPTION = {
  HALF: "HALF",
  FULL: "FULL",
} as const;

export const EXTRA_ROTI = {
  MIN: 0,
  MAX: 5,
  DEFAULT: 0,
} as const;

export const MEAL_RESPONSE_MESSAGES = {
  SUBMIT_SUCCESS: "Meal response submitted successfully.",
  SUBMIT_FAILED: "Failed to submit meal response.",
};

export const MEAL_RESPONSE_FORM = {
  DEFAULT_VALUES: {
    responseStatus: null,
    mealOption: null,
    extraRotiCount: EXTRA_ROTI.DEFAULT,
  },
};
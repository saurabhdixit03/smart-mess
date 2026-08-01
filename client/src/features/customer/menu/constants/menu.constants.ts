/**
 * Menu Module Constants
 * ---------------------------------------
 * All customer menu-related constants should live here.
 * Avoid hardcoding values anywhere else.
 */

export const MENU_API_ENDPOINT = "/menus";

export const MEAL_SESSION = {
  LUNCH: "LUNCH",
  DINNER: "DINNER",
} as const;

export const MEAL_SESSION_LABELS = {
  LUNCH: "Lunch",
  DINNER: "Dinner",
} as const;

export const MENU_PAGE = {
  TITLE: "Today's Menu",
  DESCRIPTION: "View today's lunch and dinner menu.",
};

export const MENU_MESSAGES = {
  EMPTY: "No menu has been published for today.",
  FETCH_FAILED: "Failed to load today's menus.",
};
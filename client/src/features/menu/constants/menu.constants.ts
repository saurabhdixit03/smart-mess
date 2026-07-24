/**
 * Menu Module Constants
 * ---------------------------------------
 * All menu-related constants should live here.
 * Avoid hardcoding values anywhere else.
 */

export const MENU_API_ENDPOINT = "/menus";

export const MEAL_SESSION = {
  LUNCH: "LUNCH",
  DINNER: "DINNER",
} as const;

export const MENU_VALIDATION = {
  MENU_ITEM_MAX_LENGTH: 100,
};

export const MENU_MESSAGES = {
  PUBLISH_SUCCESS: "Menu published successfully.",
  PUBLISH_FAILED: "Failed to publish menu.",
};

export const MENU_FORM = {
  DEFAULT_VALUES: {
    sabjiOne: "",
    sabjiTwo: "",
    dal: "",
    rice: "",
    sweet: "",
  },
};
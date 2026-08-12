import { z } from "zod";

import { MENU_VALIDATION } from "../constants/menu.constants";

export const menuSchema = z.object({
  sabjiOne: z
    .string()
    .trim()
    .min(1, "First sabji is required")
    .max(
      MENU_VALIDATION.MENU_ITEM_MAX_LENGTH,
      `First sabji cannot exceed ${MENU_VALIDATION.MENU_ITEM_MAX_LENGTH} characters`
    ),

  sabjiTwo: z
    .string()
    .trim()
    .max(
      MENU_VALIDATION.MENU_ITEM_MAX_LENGTH,
      `Second sabji cannot exceed ${MENU_VALIDATION.MENU_ITEM_MAX_LENGTH} characters`
    ),

  dal: z
    .string()
    .trim()
    .max(
      MENU_VALIDATION.MENU_ITEM_MAX_LENGTH,
      `Dal cannot exceed ${MENU_VALIDATION.MENU_ITEM_MAX_LENGTH} characters`
    ),

  rice: z
    .string()
    .trim()
    .max(
      MENU_VALIDATION.MENU_ITEM_MAX_LENGTH,
      `Rice cannot exceed ${MENU_VALIDATION.MENU_ITEM_MAX_LENGTH} characters`
    ),

  sweet: z
    .string()
    .trim()
    .max(
      MENU_VALIDATION.MENU_ITEM_MAX_LENGTH,
      `Sweet cannot exceed ${MENU_VALIDATION.MENU_ITEM_MAX_LENGTH} characters`
    ),
});

export type PublishMenuFormData = z.infer<typeof menuSchema>;
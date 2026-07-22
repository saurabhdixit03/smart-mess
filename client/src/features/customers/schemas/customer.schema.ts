import { z } from "zod";

import { CUSTOMER_VALIDATION } from "../constants/customer.constants";

export const customerSchema = z.object({
  fullName: z
    .string()
    .trim()
    .min(1, "Full name is required")
    .max(
      CUSTOMER_VALIDATION.FULL_NAME_MAX_LENGTH,
      `Full name cannot exceed ${CUSTOMER_VALIDATION.FULL_NAME_MAX_LENGTH} characters`
    ),

  mobileNumber: z
    .string()
    .regex(
      /^[6-9]\d{9}$/,
      "Mobile number must be a valid 10-digit Indian mobile number"
    ),

  email: z
    .string()
    .trim()
    .email("Invalid email address")
    .max(
      CUSTOMER_VALIDATION.EMAIL_MAX_LENGTH,
      `Email cannot exceed ${CUSTOMER_VALIDATION.EMAIL_MAX_LENGTH} characters`
    )
    .or(z.literal("")),

  remarks: z
    .string()
    .max(
      CUSTOMER_VALIDATION.REMARKS_MAX_LENGTH,
      `Remarks cannot exceed ${CUSTOMER_VALIDATION.REMARKS_MAX_LENGTH} characters`
    ),
});

export type CustomerFormData = z.infer<typeof customerSchema>;
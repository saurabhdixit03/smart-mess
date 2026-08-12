
import { z } from "zod";

import { CUSTOMER_VALIDATION } from "../constants/customer.constants";

export const customerSchema = z.object({
  remarks: z
    .string()
    .max(
      CUSTOMER_VALIDATION.REMARKS_MAX_LENGTH,
      `Remarks cannot exceed ${CUSTOMER_VALIDATION.REMARKS_MAX_LENGTH} characters`
    ),
});

export type CustomerFormData = z.infer<typeof customerSchema>;

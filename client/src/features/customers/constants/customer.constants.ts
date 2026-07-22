/**
 * Customer Module Constants
 * ---------------------------------------
 * All customer-related constants should live here.
 * Avoid hardcoding values anywhere else.
 */



export const CUSTOMER_API_ENDPOINT = "/customers";

export const CUSTOMER_STATUS = {
  ACTIVE: "ACTIVE",
  INACTIVE: "INACTIVE",
} as const;

export const CUSTOMER_VALIDATION = {
  FULL_NAME_MAX_LENGTH: 100,
  EMAIL_MAX_LENGTH: 100,
  REMARKS_MAX_LENGTH: 500,
  MOBILE_NUMBER_LENGTH: 10,
};

export const CUSTOMER_MESSAGES = {
  CREATE_SUCCESS: "Customer created successfully.",
  UPDATE_SUCCESS: "Customer updated successfully.",
  DELETE_SUCCESS: "Customer deactivated successfully.",
};

export const CUSTOMER_FORM = {
  DEFAULT_VALUES: {
    fullName: "",
    mobileNumber: "",
    email: "",
    remarks: "",
  },
};
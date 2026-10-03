import { useState } from "react";

import {
  authApi,
} from "../api/auth.api";

import type {
  CustomerRegistrationRequest,
} from "../types/auth.types";

export function useCustomerRegistration() {
  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const register = async (
    payload: CustomerRegistrationRequest
  ) => {
    try {
      setLoading(true);
      setError(null);

      const response =
        await authApi.customerRegister(payload);

      /*
       * Pending registration does not authenticate the customer.
       * Login becomes available after the owner approves them.
       */
      return response.data;
    } catch (registrationError) {
      const message =
        registrationError instanceof Error
          ? registrationError.message
          : "Registration failed.";

      setError(message);

      throw registrationError;
    } finally {
      setLoading(false);
    }
  };

  return {
    register,
    loading,
    error,
  };
}
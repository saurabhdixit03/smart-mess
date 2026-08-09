import { useState } from "react";

import { authApi } from "../api/auth.api";
import { saveCustomerAuthSession } from "../utils/auth.utils";

import type {
  CustomerRegistrationRequest,
} from "../types/auth.types";

export function useCustomerRegistration() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const register = async (
    payload: CustomerRegistrationRequest
  ) => {
    try {
      setLoading(true);
      setError(null);

      const response = await authApi.customerRegister(payload);

      saveCustomerAuthSession(response.data); 

      return response.data;
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "Registration failed.";

      setError(message);

      throw error;
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
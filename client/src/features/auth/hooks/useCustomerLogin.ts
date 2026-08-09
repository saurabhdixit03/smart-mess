import { useState } from "react";

import { authApi } from "../api/auth.api";
import { saveCustomerAuthSession } from "../utils/auth.utils";

import { toast } from "sonner";

import type {
  CustomerLoginRequest,
} from "../types/auth.types";

export function useCustomerLogin() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const login = async (
    payload: CustomerLoginRequest
  ) => {
    try {
      setLoading(true);
      setError(null);

      const response = await authApi.customerLogin(payload);

      saveCustomerAuthSession(response.data);

      toast.success("Login successful.");

      return response.data;
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "Login failed.";

      setError(message);

      throw error;
    } finally {
      setLoading(false);
    }
  };

  return {
    login,
    loading,
    error,
  };
}
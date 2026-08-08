import { useState } from "react";

import { authApi } from "../api/auth.api";

import type { OwnerRegistrationRequest } from "../types/auth.types";

export function useOwnerRegistration() {
  const [loading, setLoading] = useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const register = async (
    payload: OwnerRegistrationRequest
  ) => {
    try {
      setLoading(true);
      setError(null);

      const response =
        await authApi.register(payload);

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
import { useState } from "react";

import { authApi } from "../api/auth.api";
import { saveAuthSession } from "../utils/auth.utils";

import type { OwnerLoginRequest } from "../types/auth.types";

export function useOwnerLogin() {
  const [loading, setLoading] = useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const login = async (
    payload: OwnerLoginRequest
  ) => {
    try {
      setLoading(true);
      setError(null);

      const response =
        await authApi.login(payload);

      saveAuthSession(response.data);

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
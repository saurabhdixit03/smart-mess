import { useState } from "react";
import { toast } from "sonner";

import { authApi } from "../api/auth.api";

import type {
  AuthRole,
  ForgotPasswordRequest,
} from "../types/auth.types";

export function useForgotPassword(
  role: AuthRole
) {
  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const [success, setSuccess] =
    useState(false);

  const requestPasswordReset = async (
    payload: ForgotPasswordRequest
  ) => {
    try {
      setLoading(true);
      setError(null);
      setSuccess(false);

      const response =
        role === "OWNER"
          ? await authApi.ownerForgotPassword(
              payload
            )
          : await authApi.customerForgotPassword(
              payload
            );

      setSuccess(true);

      toast.success(
        "Password reset instructions sent."
      );

      return response.data;

    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "Unable to request password reset.";

      setError(message);

      throw error;

    } finally {
      setLoading(false);
    }
  };

  return {
    requestPasswordReset,
    loading,
    error,
    success,
  };
}
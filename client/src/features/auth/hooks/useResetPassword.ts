import { useState } from "react";
import { toast } from "sonner";

import { authApi } from "../api/auth.api";

import type {
  ResetPasswordRequest,
} from "../types/auth.types";

export function useResetPassword() {
  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const [success, setSuccess] =
    useState(false);

  const resetPassword = async (
    payload: ResetPasswordRequest
  ) => {
    try {
      setLoading(true);
      setError(null);
      setSuccess(false);

      const response =
        await authApi.resetPassword(payload);

      setSuccess(true);

      toast.success(
        "Password reset successfully."
      );

      return response.data;

    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "Unable to reset password.";

      setError(message);

      throw error;

    } finally {
      setLoading(false);
    }
  };

  return {
    resetPassword,
    loading,
    error,
    success,
  };
}
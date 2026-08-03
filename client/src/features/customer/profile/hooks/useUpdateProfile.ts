import { useState } from "react";

import { profileApi } from "../api";

import type {
  CustomerProfile,
  UpdateProfileRequest,
} from "../types";

export function useUpdateProfile() {
  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  async function updateProfile(
    customerId: number,
    request: UpdateProfileRequest
  ): Promise<CustomerProfile | null> {
    try {
      setLoading(true);

      setError(null);

      const response =
        await profileApi.updateProfile(
          customerId,
          request
        );

      return response.data;

    } catch {

      setError(
        "Failed to update profile."
      );

      return null;

    } finally {

      setLoading(false);

    }
  }

  return {
    updateProfile,
    loading,
    error,
  };
}
import { useCallback, useEffect, useState } from "react";

import { profileApi } from "../api";

import type { CustomerProfile } from "../types";

export function useProfile(
  customerId: number
) {
  const [profile, setProfile] =
    useState<CustomerProfile | null>(
      null
    );

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const fetchProfile =
    useCallback(async () => {
      try {
        setLoading(true);

        setError(null);

        const response =
          await profileApi.getProfile(
            customerId
          );

        setProfile(
          response.data
        );
      } catch {
        setError(
          "Failed to load profile."
        );
      } finally {
        setLoading(false);
      }
    }, [customerId]);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  return {
    profile,
    loading,
    error,
    fetchProfile,
  };
}
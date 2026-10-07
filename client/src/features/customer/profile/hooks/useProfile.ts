import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { profileApi } from "../api";

import type { CustomerProfile } from "../types";

type ProfileState = {
  customerId: number;
  profile: CustomerProfile | null;
  error: string | null;
};

export function useProfile(
  customerId: number
) {
  const [state, setState] =
    useState<ProfileState | null>(null);

  const [refreshing, setRefreshing] =
    useState(false);

  const requestVersionRef = useRef(0);

  const loadProfile = useCallback(
    (): Promise<void> => {
      const requestVersion =
        ++requestVersionRef.current;

      return profileApi
        .getProfile(customerId)
        .then((response) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setState({
            customerId,
            profile: response.data,
            error: null,
          });
        })
        .catch(() => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setState((previous) => ({
            customerId,
            profile:
              previous !== null &&
              previous.customerId === customerId
                ? previous.profile
                : null,
            error: "Failed to load profile.",
          }));
        })
        .finally(() => {
          if (
            requestVersion === requestVersionRef.current
          ) {
            setRefreshing(false);
          }
        });
    },
    [customerId]
  );

  const fetchProfile = useCallback(
    (): Promise<void> => {
      setRefreshing(true);

      setState((previous) =>
        previous !== null &&
        previous.customerId === customerId
          ? { ...previous, error: null }
          : previous
      );

      return loadProfile();
    },
    [customerId, loadProfile]
  );

  useEffect(() => {
    void loadProfile();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadProfile]);

  const currentState =
    state !== null &&
    state.customerId === customerId
      ? state
      : null;

  return {
    profile: currentState?.profile ?? null,
    loading: currentState === null || refreshing,
    error: currentState?.error ?? null,
    fetchProfile,
  };
}
import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { settingsApi } from "../api";

import type { MessSettingsResponse } from "../types";

export function useSettings() {
  const [settings, setSettings] =
    useState<MessSettingsResponse | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const requestVersion = useRef(0);

  const loadSettings = useCallback((): Promise<void> => {
    const version = ++requestVersion.current;

    return settingsApi
      .getSettings()
      .then((response) => {
        if (requestVersion.current !== version) {
          return;
        }

        setSettings(response.data);
        setError(null);
      })
      .catch((err: unknown) => {
        if (requestVersion.current !== version) {
          return;
        }

        setError(
          err instanceof Error
            ? err.message
            : "Failed to load mess settings."
        );
      })
      .finally(() => {
        if (requestVersion.current === version) {
          setLoading(false);
        }
      });
  }, []);

  const refreshSettings = useCallback((): Promise<void> => {
    setLoading(true);
    setError(null);

    return loadSettings();
  }, [loadSettings]);

  useEffect(() => {
    void loadSettings();

    return () => {
      requestVersion.current += 1;
    };
  }, [loadSettings]);

  return {
    settings,
    loading,
    error,
    refreshSettings,
  };
}
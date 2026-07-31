import { useEffect, useState } from "react";

import { settingsApi } from "../api";

import type {
  MessSettingsResponse,
} from "../types";

export function useSettings() {

  const [settings, setSettings] =
    useState<MessSettingsResponse | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const fetchSettings = async () => {

    try {

      setLoading(true);
      setError(null);

      const response =
        await settingsApi.getSettings();

      setSettings(
        response.data
      );

    } catch {

      /*
       * Settings may not exist yet.
       * We'll treat it as "Create Settings"
       * instead of an application error.
       */
      setSettings(null);

    } finally {

      setLoading(false);

    }

  };

  useEffect(() => {

    fetchSettings();

  }, []);

return {
  settings,
  loading,
  error,
  refreshSettings: fetchSettings,
};
}
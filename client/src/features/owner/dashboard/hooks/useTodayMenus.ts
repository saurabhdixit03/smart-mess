import { useCallback, useEffect, useState } from "react";

import { menuApi } from "@/features/owner/menu/api/menu.api";

import type { MenuResponse } from "@/features/owner/menu/types/menu.types";

const REFRESH_INTERVAL = 5000;

export function useTodayMenus() {

  const [todayMenus, setTodayMenus] =
    useState<MenuResponse[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const fetchTodayMenus = useCallback(
    async (showLoader = true) => {

      try {

        if (showLoader) {
          setLoading(true);
        }

        setError(null);

        const response =
          await menuApi.getTodayMenus();

        setTodayMenus(response.data);

      } catch {

        setError(
          "Failed to load today's menus."
        );

      } finally {

        if (showLoader) {
          setLoading(false);
        }

      }

    },
    []
  );

  useEffect(() => {

    fetchTodayMenus(true);

    const interval = setInterval(() => {
      fetchTodayMenus(false);
    }, REFRESH_INTERVAL);

    return () => clearInterval(interval);

  }, [fetchTodayMenus]);

  return {

    todayMenus,

    loading,

    error,

    refresh: () =>
      fetchTodayMenus(true),

  };

}
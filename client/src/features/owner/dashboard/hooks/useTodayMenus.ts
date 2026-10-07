import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { menuApi } from "@/features/owner/menu/api/menu.api";

import type {
  MenuResponse,
} from "@/features/owner/menu/types/menu.types";

const REFRESH_INTERVAL = 5000;

export function useTodayMenus() {
  const [todayMenus, setTodayMenus] =
    useState<MenuResponse[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const requestVersionRef = useRef(0);

  const loadTodayMenus = useCallback((): Promise<void> => {
    const version = ++requestVersionRef.current;

    return menuApi
      .getTodayMenus()
      .then((response) => {
        if (requestVersionRef.current !== version) {
          return;
        }

        setTodayMenus(response.data);
        setError(null);
      })
      .catch(() => {
        if (requestVersionRef.current === version) {
          setError("Failed to load today's menus.");
        }
      })
      .finally(() => {
        if (requestVersionRef.current === version) {
          setLoading(false);
        }
      });
  }, []);

  const refresh = useCallback((): Promise<void> => {
    setLoading(true);
    setError(null);

    return loadTodayMenus();
  }, [loadTodayMenus]);

  useEffect(() => {
    void loadTodayMenus();

    const interval = setInterval(() => {
      void loadTodayMenus();
    }, REFRESH_INTERVAL);

    return () => {
      clearInterval(interval);
      requestVersionRef.current += 1;
    };
  }, [loadTodayMenus]);

  return {
    todayMenus,
    loading,
    error,
    refresh,
  };
}
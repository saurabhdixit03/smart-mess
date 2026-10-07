import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { menuApi } from "../api";

import type { Menu } from "../types";

export function useMenus() {
  const [todayMenus, setTodayMenus] =
    useState<Menu[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const requestVersionRef = useRef(0);

  const loadTodayMenus = useCallback(
    (): Promise<void> => {
      const requestVersion =
        ++requestVersionRef.current;

      return menuApi
        .getTodayMenus()
        .then((response) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setTodayMenus(response.data);
          setError(null);
        })
        .catch(() => {
          if (
            requestVersion === requestVersionRef.current
          ) {
            setError("Failed to load today's menus.");
          }
        })
        .finally(() => {
          if (
            requestVersion === requestVersionRef.current
          ) {
            setLoading(false);
          }
        });
    },
    []
  );

  const fetchTodayMenus = useCallback(
    (): Promise<void> => {
      setLoading(true);
      setError(null);

      return loadTodayMenus();
    },
    [loadTodayMenus]
  );

  useEffect(() => {
    void loadTodayMenus();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadTodayMenus]);

  return {
    todayMenus,
    loading,
    error,
    fetchTodayMenus,
  };
}
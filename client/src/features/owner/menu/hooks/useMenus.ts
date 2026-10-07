import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { menuApi } from "../api/menu.api";

import type {
  MenuAvailabilityResponse,
  MenuResponse,
} from "../types/menu.types";

export function useMenus() {
  const [todayMenus, setTodayMenus] =
    useState<MenuResponse[]>([]);

  const [availability, setAvailability] =
    useState<MenuAvailabilityResponse[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const requestVersionRef = useRef(0);

  const loadTodayMenus = useCallback((): Promise<void> => {
    const version = ++requestVersionRef.current;

    return Promise.all([
      menuApi.getTodayMenus(),
      menuApi.getTodayMenuAvailability(),
    ])
      .then(([menusResponse, availabilityResponse]) => {
        if (requestVersionRef.current !== version) {
          return;
        }

        setTodayMenus(menusResponse.data);
        setAvailability(availabilityResponse.data);
        setError(null);
      })
      .catch(() => {
        if (requestVersionRef.current === version) {
          setError(
            "Failed to load today's menu information."
          );
        }
      })
      .finally(() => {
        if (requestVersionRef.current === version) {
          setLoading(false);
        }
      });
  }, []);

  const fetchTodayMenus = useCallback((): Promise<void> => {
    setLoading(true);
    setError(null);

    return loadTodayMenus();
  }, [loadTodayMenus]);

  useEffect(() => {
    void loadTodayMenus();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadTodayMenus]);

  return {
    todayMenus,
    availability,
    loading,
    error,
    fetchTodayMenus,
  };
}
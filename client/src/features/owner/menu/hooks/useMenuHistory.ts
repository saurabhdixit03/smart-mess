import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { menuApi } from "../api/menu.api";

import type {
  MenuResponse,
} from "../types/menu.types";

export function useMenuHistory() {
  const [menuHistory, setMenuHistory] =
    useState<MenuResponse[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const requestVersionRef = useRef(0);

  const loadMenuHistory = useCallback((): Promise<void> => {
    const version = ++requestVersionRef.current;

    return menuApi
      .getMenuHistory()
      .then((response) => {
        if (requestVersionRef.current !== version) {
          return;
        }

        setMenuHistory(response.data);
        setError(null);
      })
      .catch(() => {
        if (requestVersionRef.current === version) {
          setError("Failed to load menu history.");
        }
      })
      .finally(() => {
        if (requestVersionRef.current === version) {
          setLoading(false);
        }
      });
  }, []);

  const fetchMenuHistory = useCallback((): Promise<void> => {
    setLoading(true);
    setError(null);

    return loadMenuHistory();
  }, [loadMenuHistory]);

  useEffect(() => {
    void loadMenuHistory();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadMenuHistory]);

  return {
    menuHistory,
    loading,
    error,
    fetchMenuHistory,
  };
}
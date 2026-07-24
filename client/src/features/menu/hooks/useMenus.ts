import { useEffect, useState } from "react";

import { menuApi } from "../api/menu.api";
import type { MenuResponse } from "../types/menu.types";

export function useMenus() {
  const [todayMenus, setTodayMenus] = useState<MenuResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTodayMenus = async () => {
    try {
      setLoading(true);
      setError(null);

      const apiResponse =
        await menuApi.getTodayMenus();

      setTodayMenus(apiResponse.data);

    } catch {

      setError(
        "Failed to load today's menus."
      );

    } finally {

      setLoading(false);

    }
  };

  useEffect(() => {
    fetchTodayMenus();
  }, []);

  return {
    todayMenus,
    loading,
    error,
    fetchTodayMenus,
  };
}
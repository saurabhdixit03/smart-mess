import { useEffect, useState } from "react";

import { menuApi } from "../api/menu.api";

import type {
  MenuAvailabilityResponse,
  MenuResponse,
} from "../types/menu.types";

export function useMenus() {
  const [todayMenus, setTodayMenus] = useState<MenuResponse[]>([]);
  const [availability, setAvailability] = useState<MenuAvailabilityResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTodayMenus = async () => {
    try {
      setLoading(true);
      setError(null);

      const [
        menusResponse,
        availabilityResponse,
      ] = await Promise.all([
        menuApi.getTodayMenus(),
        menuApi.getTodayMenuAvailability(),
      ]);

      setTodayMenus(
        menusResponse.data
      );

      setAvailability(
        availabilityResponse.data
      );

    } catch {

      setError(
        "Failed to load today's menu information."
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
    availability,
    loading,
    error,
    fetchTodayMenus,
  };
}
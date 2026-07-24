import { useEffect, useState } from "react";

import { menuApi } from "../api/menu.api";

import type { MenuResponse } from "../types/menu.types";

export function useMenuHistory() {

  const [menuHistory, setMenuHistory] =
    useState<MenuResponse[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const fetchMenuHistory = async () => {

    try {

      setLoading(true);

      setError(null);

      const apiResponse =
        await menuApi.getMenuHistory();

      setMenuHistory(
        apiResponse.data
      );

    } catch {

      setError(
        "Failed to load menu history."
      );

    } finally {

      setLoading(false);

    }

  };

  useEffect(() => {

    fetchMenuHistory();

  }, []);

  return {

    menuHistory,

    loading,

    error,

    fetchMenuHistory,

  };

}
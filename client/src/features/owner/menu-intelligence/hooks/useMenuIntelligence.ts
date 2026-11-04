
import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { menuIntelligenceApi } from "../api";

import type {
  DishIntelligence,
  MenuIntelligenceOverview,
} from "../types";

type MenuIntelligenceState = {
  dishes: DishIntelligence[];
  overview: MenuIntelligenceOverview | null;
  loading: boolean;
  error: string | null;
};

const initialState: MenuIntelligenceState = {
  dishes: [],
  overview: null,
  loading: true,
  error: null,
};

export function useMenuIntelligence() {
  const requestVersionRef = useRef(0);

  const [state, setState] =
    useState<MenuIntelligenceState>(initialState);

  const loadMenuIntelligence = useCallback(
    (): Promise<void> => {
      const version = ++requestVersionRef.current;

      return Promise.all([
        menuIntelligenceApi.getDishes(),
        menuIntelligenceApi.getOverview(),
      ])
        .then(([dishes, overview]) => {
          if (version !== requestVersionRef.current) {
            return;
          }

          setState({
            dishes,
            overview,
            loading: false,
            error: null,
          });
        })
        .catch((error: unknown) => {
          if (version !== requestVersionRef.current) {
            return;
          }

          setState({
            ...initialState,
            loading: false,
            error:
              error instanceof Error
                ? error.message
                : "Unable to load menu intelligence. Please try again.",
          });
        });
    },
    []
  );

  const refresh = useCallback((): Promise<void> => {
    setState((previous) => ({
      ...previous,
      loading: true,
      error: null,
    }));

    return loadMenuIntelligence();
  }, [loadMenuIntelligence]);

  useEffect(() => {
    void loadMenuIntelligence();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadMenuIntelligence]);

  return {
    dishes: state.dishes,
    overview: state.overview,
    loading: state.loading,
    error: state.error,
    refresh,
  };
}

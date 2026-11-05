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

  const [analyzing, setAnalyzing] = useState(false);

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

  const analyze = useCallback(async (): Promise<void> => {
    setAnalyzing(true);
    setState((previous) => ({
      ...previous,
      error: null,
    }));

    try {
      await menuIntelligenceApi.analyzeMenuIntelligence();
      await refresh();
    } catch (error: unknown) {
      setState((previous) => ({
        ...previous,
        error:
          error instanceof Error
            ? error.message
            : "Unable to generate AI menu intelligence. Please try again.",
      }));

      throw error;
    } finally {
      setAnalyzing(false);
    }
  }, [refresh]);

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
    analyzing,
    analyze,
    refresh,
  };
}
import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { messClosureApi } from "@/features/owner/settings/api/messClosure.api";

import type {
  MessClosureResponse,
} from "@/features/owner/settings/types";

export function useCustomerClosure() {
  const [closure, setClosure] =
    useState<MessClosureResponse | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const requestVersionRef = useRef(0);

  const loadClosure = useCallback(
    (): Promise<void> => {
      const requestVersion =
        ++requestVersionRef.current;

      return messClosureApi
        .getCurrentAndUpcomingClosures()
        .then((response) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setClosure(response.data[0] ?? null);
          setError(null);
        })
        .catch((err: unknown) => {
          if (
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          console.error(
            "Failed to load customer closure notice.",
            err
          );

          setClosure(null);
          setError(
            "Failed to load closure information."
          );
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

  const fetchClosure = useCallback(
    (): Promise<void> => {
      setLoading(true);
      setError(null);

      return loadClosure();
    },
    [loadClosure]
  );

  useEffect(() => {
    void loadClosure();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadClosure]);

  return {
    closure,
    loading,
    error,
    refreshClosure: fetchClosure,
  };
}
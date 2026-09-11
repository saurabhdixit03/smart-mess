import {
  useCallback,
  useEffect,
  useState,
} from "react";

import { messClosureApi } from "@/features/owner/settings/api/messClosure.api";

import type { MessClosureResponse } from "@/features/owner/settings/types";

export function useCustomerClosure() {
  const [
    closure,
    setClosure,
  ] = useState<MessClosureResponse | null>(
    null
  );

  const [
    loading,
    setLoading,
  ] = useState(true);

  const [
    error,
    setError,
  ] = useState<string | null>(
    null
  );

  const fetchClosure =
    useCallback(async () => {
      try {
        setLoading(true);
        setError(null);

        const response =
          await messClosureApi
            .getCurrentAndUpcomingClosures();

        const closures =
          response.data;

        setClosure(
          closures.length > 0
            ? closures[0]
            : null
        );
      } catch (err) {
        console.error(
          "Failed to load customer closure notice.",
          err
        );

        setClosure(null);
        setError(
          "Failed to load closure information."
        );
      } finally {
        setLoading(false);
      }
    }, []);

  useEffect(() => {
    fetchClosure();
  }, [fetchClosure]);

  return {
    closure,
    loading,
    error,
    refreshClosure: fetchClosure,
  };
}
import {
  useCallback,
  useEffect,
  useState,
} from "react";

import { getCollectionQueue } from "../api";

import type {
  CollectionQueueItem,
  MealSession,
} from "../types";

export function useRecordQueue(
  mealSession: MealSession,
  enabled = true
) {
  const [recordQueue, setRecordQueue] =
    useState<CollectionQueueItem[]>([]);

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const refetch = useCallback(async () => {
    if (!enabled) {
      setRecordQueue([]);
      setLoading(false);
      setError(null);
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const response =
        await getCollectionQueue(
          mealSession
        );

      setRecordQueue(
        response.data
      );
    } catch {
      setError(
        "Failed to load meal record queue."
      );
    } finally {
      setLoading(false);
    }
  }, [mealSession, enabled]);

  useEffect(() => {
    if (!enabled) {
      setRecordQueue([]);
      setLoading(false);
      setError(null);
      return;
    }

    refetch();
  }, [
    refetch,
    enabled,
  ]);

  return {
    recordQueue,
    loading,
    error,
    refetch,
  };
}
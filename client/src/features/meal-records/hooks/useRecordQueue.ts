import { useCallback, useEffect, useState } from "react";

import { getCollectionQueue } from "../api";

import type {
  CollectionQueueItem,
  MealSession,
} from "../types";

export function useRecordQueue(
  mealSession: MealSession
) {

  const [recordQueue, setRecordQueue] =
    useState<CollectionQueueItem[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const refetch = useCallback(async () => {

    try {

      setLoading(true);

      const response =
        await getCollectionQueue(mealSession);

      setRecordQueue(response.data);

      setError(null);

    } catch {

      setError(
        "Failed to load meal record queue."
      );

    } finally {

      setLoading(false);

    }

  }, [mealSession]);

  useEffect(() => {

    refetch();

  }, [refetch]);

  return {

    recordQueue,

    loading,

    error,

    refetch,

  };

}
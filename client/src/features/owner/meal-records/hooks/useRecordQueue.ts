import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { getCollectionQueue } from "../api";

import type {
  CollectionQueueItem,
  MealSession,
} from "../types";

type QueueState = {
  key: string;
  recordQueue: CollectionQueueItem[];
  loading: boolean;
  error: string | null;
};

export function useRecordQueue(
  mealSession: MealSession,
  enabled = true
) {
  const [state, setState] =
    useState<QueueState | null>(null);

  const requestVersionRef = useRef(0);

  const stateKey = `${mealSession}-${enabled}`;

  const loadQueue = useCallback(
    (): Promise<void> => {
      const version = ++requestVersionRef.current;

      if (!enabled) {
        return Promise.resolve();
      }

      return getCollectionQueue(mealSession)
        .then((response) => {
          if (requestVersionRef.current !== version) {
            return;
          }

          setState({
            key: stateKey,
            recordQueue: response.data,
            loading: false,
            error: null,
          });
        })
        .catch(() => {
          if (requestVersionRef.current !== version) {
            return;
          }

          setState((previous) => ({
            key: stateKey,
            recordQueue:
              previous?.key === stateKey
                ? previous.recordQueue
                : [],
            loading: false,
            error: "Failed to load meal record queue.",
          }));
        });
    },
    [mealSession, enabled, stateKey]
  );

  const refetch = useCallback((): Promise<void> => {
    if (!enabled) {
      setState(null);
    } else {
      setState((previous) => ({
        key: stateKey,
        recordQueue:
          previous?.key === stateKey
            ? previous.recordQueue
            : [],
        loading: true,
        error: null,
      }));
    }

    return loadQueue();
  }, [enabled, stateKey, loadQueue]);

  useEffect(() => {
    if (!enabled) {
      return;
    }

    void loadQueue();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [enabled, loadQueue]);

  const currentState =
    state?.key === stateKey ? state : null;

  return {
    recordQueue:
      enabled
        ? currentState?.recordQueue ?? []
        : [],

    loading:
      enabled &&
      (currentState === null || currentState.loading),

    error:
      enabled
        ? currentState?.error ?? null
        : null,

    refetch,
  };
}
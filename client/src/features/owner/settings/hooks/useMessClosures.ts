import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { toast } from "sonner";

import { messClosureApi } from "../api";

import type {
  CreateMessClosureRequest,
  MessClosureResponse,
  UpdateMessClosureRequest,
} from "../types";

export function useMessClosures() {
  const [closures, setClosures] =
    useState<MessClosureResponse[]>([]);

  const [history, setHistory] =
    useState<MessClosureResponse[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [saving, setSaving] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const mountedRef = useRef(false);
  const closuresVersionRef = useRef(0);
  const historyVersionRef = useRef(0);

  const loadClosures = useCallback((): Promise<void> => {
    const version = ++closuresVersionRef.current;

    return messClosureApi
      .getCurrentAndUpcomingClosures()
      .then((response) => {
        if (
          mountedRef.current &&
          closuresVersionRef.current === version
        ) {
          setClosures(response.data);
        }
      })
      .catch((err: unknown) => {
        if (
          mountedRef.current &&
          closuresVersionRef.current === version
        ) {
          console.error(err);

          setError(
            "Failed to load temporary closures."
          );
        }
      })
      .finally(() => {
        if (
          mountedRef.current &&
          closuresVersionRef.current === version
        ) {
          setLoading(false);
        }
      });
  }, []);

  const fetchClosures = useCallback((): Promise<void> => {
    if (mountedRef.current) {
      setLoading(true);
      setError(null);
    }

    return loadClosures();
  }, [loadClosures]);

  const fetchHistory = useCallback((): Promise<void> => {
    const version = ++historyVersionRef.current;

    return messClosureApi
      .getClosureHistory()
      .then((response) => {
        if (
          mountedRef.current &&
          historyVersionRef.current === version
        ) {
          setHistory(response.data);
        }
      })
      .catch((err: unknown) => {
        if (
          mountedRef.current &&
          historyVersionRef.current === version
        ) {
          console.error(err);

          setError(
            "Failed to load closure history."
          );
        }
      });
  }, []);

  useEffect(() => {
    mountedRef.current = true;

    void loadClosures();
    void fetchHistory();

    return () => {
      mountedRef.current = false;
      closuresVersionRef.current += 1;
      historyVersionRef.current += 1;
    };
  }, [loadClosures, fetchHistory]);

  async function createClosure(
    payload: CreateMessClosureRequest
  ): Promise<boolean> {
    try {
      setSaving(true);

      await messClosureApi.createClosure(payload);

      if (!mountedRef.current) {
        return true;
      }

      toast.success(
        "Temporary closure created successfully."
      );

      await Promise.all([
        fetchClosures(),
        fetchHistory(),
      ]);

      return true;
    } catch (err) {
      if (mountedRef.current) {
        console.error(err);

        toast.error(
          "Failed to create temporary closure."
        );
      }

      return false;
    } finally {
      if (mountedRef.current) {
        setSaving(false);
      }
    }
  }

  async function updateClosure(
    closureId: number,
    payload: UpdateMessClosureRequest
  ): Promise<boolean> {
    try {
      setSaving(true);

      await messClosureApi.updateClosure(
        closureId,
        payload
      );

      if (!mountedRef.current) {
        return true;
      }

      toast.success(
        "Temporary closure updated successfully."
      );

      await Promise.all([
        fetchClosures(),
        fetchHistory(),
      ]);

      return true;
    } catch (err) {
      if (mountedRef.current) {
        console.error(err);

        toast.error(
          "Failed to update temporary closure."
        );
      }

      return false;
    } finally {
      if (mountedRef.current) {
        setSaving(false);
      }
    }
  }

  async function deleteClosure(
    closureId: number
  ): Promise<boolean> {
    try {
      setSaving(true);

      await messClosureApi.deleteClosure(closureId);

      if (!mountedRef.current) {
        return true;
      }

      toast.success(
        "Temporary closure deleted successfully."
      );

      await Promise.all([
        fetchClosures(),
        fetchHistory(),
      ]);

      return true;
    } catch (err) {
      if (mountedRef.current) {
        console.error(err);

        toast.error(
          "Failed to delete temporary closure."
        );
      }

      return false;
    } finally {
      if (mountedRef.current) {
        setSaving(false);
      }
    }
  }

  return {
    closures,
    history,
    loading,
    saving,
    error,
    fetchClosures,
    fetchHistory,
    createClosure,
    updateClosure,
    deleteClosure,
  };
}
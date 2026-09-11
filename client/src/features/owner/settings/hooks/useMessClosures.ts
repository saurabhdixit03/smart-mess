import {
  useCallback,
  useEffect,
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
  const [
    closures,
    setClosures,
  ] = useState<MessClosureResponse[]>([]);

  const [
    history,
    setHistory,
  ] = useState<MessClosureResponse[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [saving, setSaving] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const fetchClosures =
    useCallback(async () => {
      try {
        setLoading(true);
        setError(null);

        const response =
          await messClosureApi
            .getCurrentAndUpcomingClosures();

        setClosures(response.data);
      } catch (error) {
        console.error(error);

        setError(
          "Failed to load temporary closures."
        );
      } finally {
        setLoading(false);
      }
    }, []);

  const fetchHistory =
    useCallback(async () => {
      try {
        const response =
          await messClosureApi
            .getClosureHistory();

        setHistory(response.data);
      } catch (error) {
        console.error(error);

        setError(
          "Failed to load closure history."
        );
      }
    }, []);

  useEffect(() => {
    fetchClosures();
    fetchHistory();
  }, [
    fetchClosures,
    fetchHistory,
  ]);

  async function createClosure(
    payload: CreateMessClosureRequest
  ) {
    try {
      setSaving(true);

      await messClosureApi.createClosure(
        payload
      );

      toast.success(
        "Temporary closure created successfully."
      );

      await Promise.all([
        fetchClosures(),
        fetchHistory(),
      ]);

      return true;
    } catch (error) {
      console.error(error);

      toast.error(
        "Failed to create temporary closure."
      );

      return false;
    } finally {
      setSaving(false);
    }
  }

  async function updateClosure(
    closureId: number,
    payload: UpdateMessClosureRequest
  ) {
    try {
      setSaving(true);

      await messClosureApi.updateClosure(
        closureId,
        payload
      );

      toast.success(
        "Temporary closure updated successfully."
      );

      await Promise.all([
        fetchClosures(),
        fetchHistory(),
      ]);

      return true;
    } catch (error) {
      console.error(error);

      toast.error(
        "Failed to update temporary closure."
      );

      return false;
    } finally {
      setSaving(false);
    }
  }

  async function deleteClosure(
    closureId: number
  ) {
    try {
      setSaving(true);

      await messClosureApi.deleteClosure(
        closureId
      );

      toast.success(
        "Temporary closure deleted successfully."
      );

      await Promise.all([
        fetchClosures(),
        fetchHistory(),
      ]);

      return true;
    } catch (error) {
      console.error(error);

      toast.error(
        "Failed to delete temporary closure."
      );

      return false;
    } finally {
      setSaving(false);
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
import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import { customerApi } from "../api/customer.api";

import type {
  CustomerResponse,
} from "../types/customer.types";

export function useCustomers() {
  const [customers, setCustomers] =
    useState<CustomerResponse[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const requestVersionRef = useRef(0);

  const loadCustomers = useCallback((): Promise<void> => {
    const version = ++requestVersionRef.current;

    return customerApi
      .getAllCustomers()
      .then((response) => {
        if (requestVersionRef.current !== version) {
          return;
        }

        setCustomers(response.data);
        setError(null);
      })
      .catch(() => {
        if (requestVersionRef.current === version) {
          setError("Failed to load customers.");
        }
      })
      .finally(() => {
        if (requestVersionRef.current === version) {
          setLoading(false);
        }
      });
  }, []);

  const fetchCustomers = useCallback((): Promise<void> => {
    setLoading(true);
    setError(null);

    return loadCustomers();
  }, [loadCustomers]);

  useEffect(() => {
    void loadCustomers();

    return () => {
      requestVersionRef.current += 1;
    };
  }, [loadCustomers]);

  return {
    customers,
    loading,
    error,
    fetchCustomers,
  };
}
import { useEffect, useState } from "react";

import { customerApi } from "../api/customer.api";
import type { CustomerResponse } from "../types/customer.types";

export function useCustomers() {
  const [customers, setCustomers] = useState<CustomerResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchCustomers = async () => {
    try {
      setLoading(true);
      setError(null);

      const apiResponse = await customerApi.getAllCustomers();

      setCustomers(apiResponse.data);

    } catch {
      setError("Failed to load customers.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCustomers();
  }, []);

  return {
    customers,
    loading,
    error,
    fetchCustomers,
  };
}
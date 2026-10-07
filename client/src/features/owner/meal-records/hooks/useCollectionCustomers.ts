import {
  useEffect,
  useRef,
  useState,
} from "react";

import { searchCollectionCustomers } from "../api";

import type {
  CollectionCustomer,
  MealSession,
} from "../types";

type SearchState = {
  key: string;
  customers: CollectionCustomer[];
  loading: boolean;
  error: string | null;
};

const SEARCH_DELAY_MS = 300;
const MAX_SEARCH_LENGTH = 100;

export function useCollectionCustomers(
  mealSession: MealSession,
  search: string,
  enabled = true
) {
  const keyword = search.trim();

  const [refreshVersion, setRefreshVersion] =
    useState(0);

  const [state, setState] =
    useState<SearchState | null>(null);

  const requestVersionRef = useRef(0);

  const validSearch =
    enabled &&
    keyword.length > 0 &&
    keyword.length <= MAX_SEARCH_LENGTH;

  const searchKey = JSON.stringify([
    mealSession,
    keyword,
    enabled,
    refreshVersion,
  ]);

  useEffect(() => {
    const requestVersion =
      ++requestVersionRef.current;

    if (!validSearch) {
      return;
    }

    let disposed = false;

    const timer = setTimeout(() => {
      setState({
        key: searchKey,
        customers: [],
        loading: true,
        error: null,
      });

      void searchCollectionCustomers(
        mealSession,
        keyword
      )
        .then((response) => {
          if (
            disposed ||
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setState({
            key: searchKey,
            customers: response.data,
            loading: false,
            error: null,
          });
        })
        .catch((error: unknown) => {
          if (
            disposed ||
            requestVersion !== requestVersionRef.current
          ) {
            return;
          }

          setState({
            key: searchKey,
            customers: [],
            loading: false,
            error:
              error instanceof Error
                ? error.message
                : "Unable to search customers.",
          });
        });
    }, SEARCH_DELAY_MS);

    return () => {
      disposed = true;
      clearTimeout(timer);
      requestVersionRef.current += 1;
    };
  }, [
    mealSession,
    keyword,
    searchKey,
    validSearch,
  ]);

  function refresh() {
    setRefreshVersion((version) => version + 1);
  }

  const currentState =
    state?.key === searchKey ? state : null;

  const error =
    enabled && keyword.length > MAX_SEARCH_LENGTH
      ? "Search must not exceed 100 characters."
      : validSearch
        ? currentState?.error ?? null
        : null;

  return {
    customers:
      validSearch && currentState
        ? currentState.customers
        : [],
    loading:
      validSearch &&
      (
        currentState === null ||
        currentState.loading
      ),
    error,
    refresh,
  };
}
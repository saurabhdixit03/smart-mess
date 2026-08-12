import { useMemo, useState } from "react";

import {
  PageHeader,
} from "@/components/common/ui";

import {
  MealRecordGrid,
  MealSearchToolbar,
} from "../components";

import { useMealRecords } from "../hooks";

import { getCustomer } from "@/features/auth/utils/auth.utils";

export default function MyMealsPage() {
  const customer = getCustomer();

if (!customer) {
  return (
    <p className="text-red-500">
      Customer session not found.
    </p>
  );
}

const {
  mealRecords,
  loading,
  error,
  fetchMealRecords,
} = useMealRecords(customer.customerId);

  const [search, setSearch] =
    useState("");

  const filteredMealRecords =
    useMemo(() => {

      const query =
        search.trim().toLowerCase();

      if (!query) {
        return mealRecords;
      }

      return mealRecords.filter(
        (mealRecord) => {

          const collectedDate =
            new Date(
              mealRecord.collectedAt
            )
              .toLocaleDateString(
                "en-IN",
                {
                  day: "2-digit",
                  month: "short",
                  year: "numeric",
                }
              )
              .toLowerCase();

          const mealSession =
            mealRecord.mealSession.toLowerCase();

          return (
            collectedDate.includes(query) ||
            mealSession.includes(query)
          );

        }
      );

    }, [mealRecords, search]);

  return (
    <div className="space-y-6">

      <PageHeader
        title="My Meals"
        description="View your collected meal history."
      />

      <MealSearchToolbar
        search={search}
        onSearchChange={setSearch}
      />

      <MealRecordGrid
        mealRecords={
          filteredMealRecords
        }
        loading={loading}
        error={error}
        onRefresh={fetchMealRecords}
      />

    </div>
  );
}
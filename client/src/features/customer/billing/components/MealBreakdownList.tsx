import { useMemo, useState } from "react";

import { Search } from "lucide-react";

import { Input } from "@/components/common/ui";

import type { MealRecord } from "../types";

interface MealBreakdownListProps {
  mealRecords: MealRecord[];
}

export default function MealBreakdownList({
  mealRecords,
}: MealBreakdownListProps) {
  const [search, setSearch] =
    useState("");

  const filteredMeals =
    useMemo(() => {
      const query =
        search.toLowerCase();

      return mealRecords.filter(
        (meal) =>
          meal.collectedAt
            .toLowerCase()
            .includes(query)
      );
    }, [
      mealRecords,
      search,
    ]);

  return (
    <div className="space-y-4">

      <Input
        fullWidth
        placeholder="Search by date..."
        value={search}
        onChange={(e) =>
          setSearch(
            e.target.value
          )
        }
        leftIcon={
          <Search size={18} />
        }
      />

      <div
        className="
          max-h-[420px]
          overflow-y-auto
          rounded-xl
          border
          border-[var(--color-border)]
        "
      >

        {filteredMeals.length === 0 && (

          <div className="py-12 text-center text-sm text-[var(--color-text-secondary)]">

            No meal records found.

          </div>

        )}

      </div>

      <div
  className="
  h-[240px]
  overflow-y-auto
  rounded-xl
  border
  border-[var(--color-border)]
  divide-y
  divide-[var(--color-border)]
"
>
  {filteredMeals.length === 0 ? (

    <div className="py-12 text-center text-sm text-[var(--color-text-secondary)]">
      No meal records found.
    </div>

  ) : (

    filteredMeals.map((meal) => (
      <div
        key={meal.mealRecordId}
        className="flex items-center justify-between px-5 py-4"
      >
        <div className="space-y-1">

          <div className="flex items-center gap-2">

            <span className="font-medium">
              {meal.mealSession === "LUNCH"
                ? "🍽 Lunch"
                : "🌙 Dinner"}
            </span>

            <span className="text-sm text-[var(--color-text-secondary)]">
              •
            </span>

            <span className="text-sm text-[var(--color-text-secondary)]">
              {meal.mealOption === "FULL"
                ? "Full Meal"
                : "Half Meal"}
            </span>

          </div>

          <div className="text-sm text-[var(--color-text-secondary)]">

            {meal.extraRotiCount > 0
              ? `+${meal.extraRotiCount} Extra ${
                  meal.extraRotiCount === 1
                    ? "Roti"
                    : "Rotis"
                }`
              : "No Extra Roti"}

          </div>

          <div className="text-xs text-[var(--color-text-secondary)]">
            {new Date(
              meal.collectedAt
            ).toLocaleDateString()}
          </div>

        </div>

        <div className="text-right">

          <div className="text-lg font-semibold">
            ₹{meal.totalAmount}
          </div>

        </div>

      </div>
    ))

  )}
</div>

    </div>
  );
}
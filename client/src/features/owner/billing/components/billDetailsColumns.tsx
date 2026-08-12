import type { Column } from "@/components/common/ui/DataTable/DataTable";
import { StatusBadge } from "@/components/common/ui";

import type { MealRecordResponse } from "../types";

export type MealRecordTableRow =
  MealRecordResponse & {
    showDate: boolean;
  };

function formatDay(date: string) {
  return new Date(date).toLocaleDateString("en-IN", {
    day: "2-digit",
  });
}

export const billDetailsColumns: Column<MealRecordTableRow>[] = [
  {
    key: "collectedAt",
    header: "Date",
    className: "font-medium whitespace-nowrap",
    render: (row) =>
      row.showDate
        ? formatDay(row.collectedAt)
        : "",
  },

  {
    key: "mealSession",
    header: "Session",
    render: (row) => (
      <StatusBadge
        label={row.mealSession}
        variant={
          row.mealSession === "LUNCH"
            ? "lunch"
            : "dinner"
        }
      />
    ),
  },

  {
    key: "mealOption",
    header: "Meal",
    render: (row) => (
      <StatusBadge
        label={row.mealOption}
        variant={
          row.mealOption === "FULL"
            ? "full"
            : "half"
        }
      />
    ),
  },

  {
    key: "extraRotiCount",
    header: "Extra Rotis",
    className: "text-center",
    headerClassName: "text-center",
  },

  {
    key: "totalAmount",
    header: "Amount",
    className: "text-right font-semibold",
    headerClassName: "text-right",
    render: (row) => `₹${row.totalAmount}`,
  },
];
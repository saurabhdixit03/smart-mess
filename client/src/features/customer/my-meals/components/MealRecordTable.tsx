import DataTable, {
  type Column,
} from "@/components/common/ui/DataTable";

import type { MealRecord } from "../types";

const currencyFormat = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

function formatDate(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return date.toLocaleDateString("en-IN", {
    timeZone: "Asia/Kolkata",
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function formatTime(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return date.toLocaleTimeString("en-IN", {
    timeZone: "Asia/Kolkata",
    hour: "2-digit",
    minute: "2-digit",
  });
}

const columns: Column<MealRecord>[] = [
  {
    key: "collectedAt",
    header: "Date",
    className: "whitespace-nowrap font-medium",
    render: (record) => formatDate(record.collectedAt),
  },
  {
    key: "mealSession",
    header: "Session",
    render: (record) =>
      record.mealSession === "LUNCH"
        ? "Lunch"
        : "Dinner",
  },
  {
    key: "mealOption",
    header: "Meal",
    render: (record) =>
      record.mealOption === "FULL"
        ? "Full"
        : "Half",
  },
  {
    key: "mealPrice",
    header: "Meal Price",
    headerClassName: "text-right",
    className: "text-right whitespace-nowrap tabular-nums",
    render: (record) =>
      currencyFormat.format(record.mealPrice),
  },
  {
    key: "extraRotiCount",
    header: "Extra Rotis",
    headerClassName: "text-right",
    className: "text-right tabular-nums",
  },
  {
    key: "totalAmount",
    header: "Total",
    headerClassName: "text-right",
    className:
      "text-right whitespace-nowrap font-medium tabular-nums",
    render: (record) =>
      currencyFormat.format(record.totalAmount),
  },
  {
    key: "collectionTime",
    header: "Collected",
    className: "whitespace-nowrap",
    render: (record) => formatTime(record.collectedAt),
  },
];

interface MealRecordTableProps {
  mealRecords: MealRecord[];
}

export default function MealRecordTable({
  mealRecords,
}: MealRecordTableProps) {
  return (
    <div className="min-w-0 overflow-x-auto">
      <DataTable
        columns={columns}
        data={mealRecords}
        rowKey={(record) => record.mealRecordId}
        className="min-w-[760px]"
      />
    </div>
  );
}
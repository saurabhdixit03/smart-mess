import DataTable, {
  type Column,
} from "@/components/common/ui/DataTable/DataTable";

import StatusBadge from "@/components/common/ui/StatusBadge/StatusBadge";

import type {
  TodayMealRecord,
} from "../types";

type MealRecordTableProps = {
  records: TodayMealRecord[];
};

export default function MealRecordTable({
  records,
}: MealRecordTableProps) {

  const columns: Column<TodayMealRecord>[] = [

    {
      key: "customerName",
      header: "Customer",
    },

    {
  key: "mealOption",
  header: "Meal",

  render: (record) => (

    <StatusBadge
      label={
        record.mealOption === "FULL"
          ? "Full Meal"
          : "Half Meal"
      }
      variant={
        record.mealOption === "FULL"
          ? "full"
          : "half"
      }
    />

  ),
},

    {
      key: "extraRotiCount",
      header: "Extra Rotis",
      headerClassName: "!text-center",
      className: "!text-center",
    },

    {
  key: "collectedAt",
  header: "Collected At",

  render: (record) =>
    new Date(record.collectedAt)
      .toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
      }),
},

  ];

  return (

    <section className="space-y-4">

      <div>

        <h2 className="text-lg font-semibold">

          Today's Meal Records

        </h2>

        <p className="text-sm text-[var(--color-text-secondary)]">

          Recently recorded meals for the selected session.

        </p>

      </div>

      <DataTable
        columns={columns}
        data={records}
        rowKey={(record) =>
          record.mealRecordId
        }
      />

    </section>

    

  );

}
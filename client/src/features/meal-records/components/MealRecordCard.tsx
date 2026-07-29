import {
  CircleCheckBig,
  Pizza,
} from "lucide-react";

import Card from "@/components/common/ui/Card/Card";
import StatusBadge from "@/components/common/ui/StatusBadge/StatusBadge";

import type { CollectionQueueItem } from "../types";

type MealRecordCardProps = {
  item: CollectionQueueItem;

  onRecord: (
    item: CollectionQueueItem
  ) => void;
};

export default function MealRecordCard({
  item,
  onRecord,
}: MealRecordCardProps) {

  return (

    <Card
      className="
        w-72
        shrink-0
        snap-start
        cursor-pointer
        p-5
        transition-all
        duration-200
        hover:-translate-y-1
        hover:border-[var(--color-primary)]
        hover:shadow-lg
      "
      onClick={() => onRecord(item)}
    >

      <div className="flex items-start justify-between">

        <div className="min-w-0">

          <p
            className="
              truncate
              text-lg
              font-semibold
              text-[var(--color-text)]
            "
          >
            {item.customerName}
          </p>

        </div>

        <CircleCheckBig
          size={22}
          className="
            shrink-0
            text-green-600
          "
        />

      </div>

      <div className="mt-5 space-y-3">

        <StatusBadge
          label={
            item.mealOption === "FULL"
              ? "Full Meal"
              : "Half Meal"
          }
          variant={
            item.mealOption === "FULL"
              ? "full"
              : "half"
          }
        />

        <div
          className="
            flex
            items-center
            gap-2
            text-sm
            text-[var(--color-text-secondary)]
          "
        >

          <Pizza
            size={16}
          />

          <span>
            Extra Rotis
          </span>

          <span className="font-semibold">
            {item.extraRotiCount}
          </span>

        </div>

      </div>

    </Card>

  );

}
import {
  CalendarDays,
  Clock3,
  Moon,
  Sun,
  UtensilsCrossed,
} from "lucide-react";

import {
  Card,
  StatusBadge,
} from "@/components/common/ui";

import type { MealRecord } from "../types";

interface MealRecordCardProps {
  mealRecord: MealRecord;
}

export default function MealRecordCard({
  mealRecord,
}: MealRecordCardProps) {

  const collectedAt = new Date(
    mealRecord.collectedAt
  );

  const date =
    collectedAt.toLocaleDateString(
      "en-IN",
      {
        day: "2-digit",
        month: "short",
        year: "numeric",
      }
    );

  const time =
    collectedAt.toLocaleTimeString(
      "en-IN",
      {
        hour: "2-digit",
        minute: "2-digit",
      }
    );

  const isLunch =
    mealRecord.mealSession === "LUNCH";

  return (
    <Card className="h-full transition-all duration-200 hover:-translate-y-1 hover:shadow-md">

      <Card.Body className="space-y-5">

        <div className="flex items-start justify-between">

          <div className="flex items-center gap-3">

            <div
              className={`rounded-xl p-3 ${
                isLunch
                  ? "bg-orange-100 text-orange-600"
                  : "bg-indigo-100 text-indigo-600"
              }`}
            >
              {isLunch ? (
                <Sun size={20} />
              ) : (
                <Moon size={20} />
              )}
            </div>

            <div>

              <h3 className="font-semibold text-[var(--color-text)]">
                {isLunch
                  ? "Lunch"
                  : "Dinner"}
              </h3>

              <p className="mt-1 flex items-center gap-1 text-xs text-[var(--color-text-secondary)]">
                <CalendarDays size={14} />

                {date}
              </p>

            </div>

          </div>

          <StatusBadge
            label={
              mealRecord.mealOption ===
              "FULL"
                ? "Full"
                : "Half"
            }
            variant={
              mealRecord.mealOption ===
              "FULL"
                ? "full"
                : "half"
            }
          />

        </div>

        <div className="space-y-3 text-sm">

          <div className="flex items-center justify-between">

            <span className="flex items-center gap-2 text-[var(--color-text-secondary)]">

              <UtensilsCrossed size={16} />

              Extra Rotis

            </span>

            <span className="font-medium">

              {mealRecord.extraRotiCount === 0
                ? "None"
                : mealRecord.extraRotiCount}

            </span>

          </div>

          <div className="flex items-center justify-between">

            <span className="flex items-center gap-2 text-[var(--color-text-secondary)]">

              <Clock3 size={16} />

              Collected

            </span>

            <span className="font-medium">
              {time}
            </span>

          </div>

        </div>

      </Card.Body>

    </Card>
  );
}
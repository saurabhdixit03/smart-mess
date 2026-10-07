import StatusBadge from "@/components/common/ui/StatusBadge/StatusBadge";

import type { MealCollectionSelection } from "../types";

type MealRecordCardProps = {
  item: MealCollectionSelection & {
    mobileNumber?: string;
    collected?: boolean;
  };

  onRecord: (item: MealCollectionSelection) => void;
};

export default function MealRecordCard({
  item,
  onRecord,
}: MealRecordCardProps) {
  const collected = item.collected === true;
  const hasResponse = item.mealResponseId !== null;

  return (
    <button
      type="button"
      disabled={collected}
      aria-label={
        collected
          ? `${item.customerName}: meal already collected`
          : `Record meal for ${item.customerName}`
      }
      onClick={() => onRecord(item)}
      className="flex h-full w-full min-w-0 flex-col rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4 text-left transition-colors enabled:cursor-pointer enabled:hover:border-[var(--color-primary)] enabled:hover:shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)] disabled:cursor-default disabled:opacity-70"
    >
      <span className="block w-full min-w-0">
        <span
          title={item.customerName}
          className="block truncate text-base font-semibold text-[var(--color-text)]"
        >
          {item.customerName}
        </span>

        {item.mobileNumber && (
          <span className="mt-1 block text-xs text-[var(--color-text-secondary)]">
            {item.mobileNumber}
          </span>
        )}
      </span>

      <span className="mt-4 block w-full border-t border-[var(--color-border)] pt-3">
        {collected ? (
          <StatusBadge
            label="Collected"
            variant="success"
          />
        ) : hasResponse ? (
          <span className="flex items-center justify-between gap-3">
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

            <span className="text-right">
              <span className="block text-xs text-[var(--color-text-secondary)]">
                Extra rotis
              </span>

              <span className="mt-0.5 block text-sm font-semibold tabular-nums text-[var(--color-text)]">
                {item.extraRotiCount}
              </span>
            </span>
          </span>
        ) : (
          <span className="text-xs text-[var(--color-text-secondary)]">
            No meal response submitted
          </span>
        )}
      </span>
    </button>
  );
}
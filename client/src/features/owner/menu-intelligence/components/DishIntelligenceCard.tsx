
import {
  Lightbulb,
  MessageSquareText,
  Star,
  Utensils,
} from "lucide-react";

import type {
  DishIntelligence,
  SuggestionPriority,
} from "../types";

type DishIntelligenceCardProps = {
  dish: DishIntelligence | null;
};

const numberFormat = new Intl.NumberFormat("en-IN");

const priorityStyles: Record<SuggestionPriority, string> = {
  HIGH: "border-red-200 bg-red-50 text-red-700",
  MEDIUM: "border-amber-200 bg-amber-50 text-amber-700",
  LOW: "border-green-200 bg-green-50 text-green-700",
};

export default function DishIntelligenceCard({
  dish,
}: DishIntelligenceCardProps) {
  if (!dish) {
    return (
      <section className="flex min-h-64 flex-col items-center justify-center rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 text-center">
        <Utensils
          size={28}
          className="mb-3 text-[var(--color-text-secondary)]"
        />

        <h2 className="font-semibold text-[var(--color-text)]">
          No dishes available
        </h2>

        <p className="mt-1 max-w-sm text-sm text-[var(--color-text-secondary)]">
          Dish intelligence will appear here once dishes are available.
        </p>
      </section>
    );
  }

  return (
    <section className="min-w-0 space-y-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
      {/* Dish Header */}
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="flex min-w-0 items-center gap-3">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[var(--color-primary)]/10 text-[var(--color-primary)]">
            <Utensils size={19} />
          </div>

          <div className="min-w-0">
            <h3 className="break-words text-base font-semibold tracking-tight text-[var(--color-text)]">
              {dish.dishName}
            </h3>

            <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
              Dish performance and customer feedback
            </p>
          </div>
        </div>

        <span
          className={`rounded-full border px-2.5 py-1 text-xs font-medium ${
            dish.active
              ? "border-green-200 bg-green-50 text-green-700"
              : "border-[var(--color-border)] bg-[var(--color-surface-hover)] text-[var(--color-text-secondary)]"
          }`}
        >
          {dish.active ? "Active" : "Inactive"}
        </span>
      </div>

      {/* Rating Summary */}
      <div className="grid grid-cols-2 gap-3">
        <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface-hover)]/40 p-4">
          <p className="text-xs text-[var(--color-text-secondary)]">
            Average customer rating
          </p>

          <div className="mt-2 flex items-center gap-2">
            <Star
              size={21}
              className="fill-amber-400 text-amber-400"
            />

            <span className="text-2xl font-bold tracking-tight text-[var(--color-text)]">
              {dish.averageRating !== null
                ? dish.averageRating.toFixed(1)
                : "—"}
            </span>

            <span className="text-sm text-[var(--color-text-secondary)]">
              / 5
            </span>
          </div>

          <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
            {numberFormat.format(dish.totalRatings)}{" "}
            {dish.totalRatings === 1 ? "rating" : "ratings"}
          </p>
        </div>

        <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface-hover)]/40 p-4">
          <p className="text-xs text-[var(--color-text-secondary)]">
            Written comments
          </p>

          <div className="mt-2 flex items-center gap-2">
            <MessageSquareText
              size={20}
              className="text-[var(--color-text-secondary)]"
            />

            <span className="text-2xl font-bold tracking-tight text-[var(--color-text)]">
              {numberFormat.format(dish.totalComments)}
            </span>
          </div>

          <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
            Customer feedback entries
          </p>
        </div>
      </div>

      {/* AI Observation and Recommendation */}
      <div className="rounded-xl border border-[var(--color-border)] p-4">
        <div className="flex items-center gap-2">
          <Lightbulb
            size={18}
            className="text-[var(--color-primary)]"
          />

          <h3 className="text-sm font-semibold text-[var(--color-text)]">
            AI Dish Intelligence
          </h3>
        </div>

        {dish.aiSuggestion ? (
          <div className="mt-3 space-y-3">
            <div>
              <div className="mb-2 flex flex-wrap items-center justify-between gap-2">
                <p className="text-xs font-medium text-[var(--color-text-secondary)]">
                  Observation
                </p>

                <span
                  className={`rounded-full border px-2.5 py-1 text-xs font-medium ${
                    priorityStyles[dish.aiSuggestion.priority]
                  }`}
                >
                  {dish.aiSuggestion.priority} priority
                </span>
              </div>

              <p className="text-sm leading-6 text-[var(--color-text)]">
                {dish.aiSuggestion.observation}
              </p>
            </div>

            <div className="border-t border-[var(--color-border)] pt-3">
              <p className="mb-1 text-xs font-medium text-[var(--color-text-secondary)]">
                Recommendation
              </p>

              <p className="text-sm leading-6 text-[var(--color-text)]">
                {dish.aiSuggestion.recommendation}
              </p>
            </div>
          </div>
        ) : (
          <p className="mt-3 text-sm leading-6 text-[var(--color-text-secondary)]">
            No AI recommendation is available for this dish yet.
            Recommendations will appear when feedback has been analysed.
          </p>
        )}
      </div>

      <p className="text-xs leading-5 text-[var(--color-text-secondary)]">
        AI recommendations are decision-support suggestions.
        The mess owner makes the final decision.
      </p>
    </section>
  );
}

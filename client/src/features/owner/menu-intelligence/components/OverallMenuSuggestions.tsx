import {
  ArrowUpRight,
  Lightbulb,
  Sparkles,
} from "lucide-react";

import type {
  MenuIntelligenceOverview,
  SuggestionPriority,
} from "../types";

type OverallMenuSuggestionsProps = {
  overview: MenuIntelligenceOverview | null;
};

function getPriorityStyles(priority: SuggestionPriority): string {
  switch (priority) {
    case "HIGH":
      return "border-red-200 bg-red-50 text-red-700";

    case "MEDIUM":
      return "border-amber-200 bg-amber-50 text-amber-700";

    case "LOW":
      return "border-[var(--color-border)] bg-[var(--color-surface-hover)] text-[var(--color-text-secondary)]";
  }
}

export default function OverallMenuSuggestions({
  overview,
}: OverallMenuSuggestionsProps) {
  const analysis = overview?.analysis;

  const overallSuggestions = (overview?.suggestions ?? []).filter(
    (suggestion) =>
      suggestion.dishName === null ||
      suggestion.dishName.trim().length === 0
  );

  return (
    <section className="interactive-surface min-w-0 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
      <div className="flex items-center gap-3">
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[var(--color-primary)]/10 text-[var(--color-primary)]">
          <Sparkles size={19} />
        </div>

        <div>
          <h2 className="text-base font-semibold tracking-tight text-[var(--color-text)]">
            Overall Meal Insights
          </h2>

          <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
            AI recommendations based on overall meal feedback.
          </p>
        </div>
      </div>

      <div className="mt-4 space-y-3">
        {!analysis ? (
          <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface-hover)]/40 px-4 py-8 text-center">
            <Lightbulb
              size={28}
              className="mx-auto mb-3 text-[var(--color-text-secondary)]"
            />

            <p className="text-sm font-semibold text-[var(--color-text)]">
              No AI analysis available yet
            </p>

            <p className="mx-auto mt-2 max-w-sm text-xs leading-5 text-[var(--color-text-secondary)]">
              Overall meal recommendations will appear here
              after feedback analysis.
            </p>
          </div>
        ) : overallSuggestions.length === 0 ? (
          <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface-hover)]/40 px-4 py-8 text-center">
            <Lightbulb
              size={28}
              className="mx-auto mb-3 text-[var(--color-text-secondary)]"
            />

            <p className="text-sm font-semibold text-[var(--color-text)]">
              No overall meal suggestions
            </p>

            <p className="mx-auto mt-2 max-w-sm text-xs leading-5 text-[var(--color-text-secondary)]">
              The latest analysis did not produce any
              overall meal recommendations.
            </p>
          </div>
        ) : (
          overallSuggestions.map((suggestion, index) => (
            <article
              key={`${suggestion.priority}-${index}`}
              className="rounded-xl border border-[var(--color-border)] p-4"
            >
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <Lightbulb
                    size={17}
                    className="text-[var(--color-primary)]"
                  />

                  <h3 className="text-sm font-semibold text-[var(--color-text)]">
                    Overall Meal Recommendation
                  </h3>
                </div>

                <span
                  className={`rounded-full border px-2.5 py-1 text-xs font-semibold ${getPriorityStyles(
                    suggestion.priority
                  )}`}
                >
                  {suggestion.priority} Priority
                </span>
              </div>

              <div className="mt-3">
                <p className="text-xs font-medium text-[var(--color-text-secondary)]">
                  Observation
                </p>

                <p className="mt-1 text-sm leading-6 text-[var(--color-text)]">
                  {suggestion.observation}
                </p>
              </div>

              <div className="mt-3 rounded-lg border border-[var(--color-primary)]/20 bg-[var(--color-primary)]/[0.04] p-3">
                <div className="flex items-center gap-2">
                  <ArrowUpRight
                    size={17}
                    className="text-[var(--color-primary)]"
                  />

                  <p className="text-xs font-semibold text-[var(--color-text)]">
                    Recommendation
                  </p>
                </div>

                <p className="mt-2 text-sm leading-6 text-[var(--color-text)]">
                  {suggestion.recommendation}
                </p>
              </div>
            </article>
          ))
        )}
      </div>

      <p className="mt-3 text-xs leading-5 text-[var(--color-text-secondary)]">
        These are AI-generated decision-support suggestions.
        Final menu decisions remain with the owner.
      </p>
    </section>
  );
}

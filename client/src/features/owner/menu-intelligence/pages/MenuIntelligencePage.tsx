import { useEffect, useMemo, useState } from "react";

import {
  ChartPie,
  LayoutGrid,
  Sparkles,
} from "lucide-react";

import {
  Button,
  Card,
  PageHeader,
} from "@/components/common/ui";

import DishIntelligenceCard from "../components/DishIntelligenceCard";
import DishIntelligenceCharts from "../components/DishIntelligenceCharts";
import { useMenuIntelligence } from "../hooks/useMenuIntelligence";

type IntelligenceView = "cards" | "charts";

export default function MenuIntelligencePage() {
  const [view, setView] = useState<IntelligenceView>("cards");

  const [selectedDishId, setSelectedDishId] =
    useState<number | null>(null);

  const {
    dishes,
    loading,
    error,
    analyzing,
    analyze,
    refresh,
  } = useMenuIntelligence();

  const suggestedDishes = useMemo(
    () =>
      dishes.filter(
        (dish) => dish.aiSuggestion !== null
      ),
    [dishes]
  );

  useEffect(() => {
    if (suggestedDishes.length === 0) {
      setSelectedDishId(null);
      return;
    }

    const selectedDishExists = suggestedDishes.some(
      (dish) => dish.dishId === selectedDishId
    );

    if (!selectedDishExists) {
      setSelectedDishId(suggestedDishes[0].dishId);
    }
  }, [suggestedDishes, selectedDishId]);

  const selectedDish =
    suggestedDishes.find(
      (dish) => dish.dishId === selectedDishId
    ) ??
    suggestedDishes[0] ??
    null;

  const hasSuggestedDishes = suggestedDishes.length > 0;

  const handleAnalyze = async () => {
    try {
      await analyze();
    } catch {
      // The hook already stores the API error in its state.
    }
  };

  return (
    <section className="space-y-4">
      <PageHeader
        title="Menu Intelligence"
        description="Explore customer ratings, dish performance, and AI-powered recommendations."
        action={
          <div className="flex w-full flex-wrap items-center justify-end gap-2 sm:w-auto">
            <Button
              type="button"
              variant="primary"
              className="shrink-0"
              disabled={analyzing || loading}
              onClick={() => void handleAnalyze()}
            >
              <Sparkles size={16} aria-hidden="true" />
              {analyzing ? "Analyzing..." : "Generate AI Insights"}
            </Button>

            {hasSuggestedDishes && (
              <Button
                type="button"
                variant="outline"
                className="shrink-0"
                title={
                  view === "cards"
                    ? "Switch to chart view"
                    : "Switch to card view"
                }
                onClick={() =>
                  setView((previous) =>
                    previous === "cards" ? "charts" : "cards"
                  )
                }
              >
                {view === "cards" ? (
                  <ChartPie size={16} aria-hidden="true" />
                ) : (
                  <LayoutGrid size={16} aria-hidden="true" />
                )}

                {view === "cards" ? "Chart View" : "Card View"}
              </Button>
            )}
          </div>
        }
      />

      {analyzing && (
        <p
          className="text-sm text-[var(--color-text-secondary)]"
          role="status"
        >
          AI is analyzing your menu and customer feedback. Please wait...
        </p>
      )}

      {loading ? (
        <div
          className="flex min-h-64 items-center justify-center rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)]"
          role="status"
        >
          <p className="text-sm text-[var(--color-text-secondary)]">
            Loading dish intelligence...
          </p>
        </div>
      ) : error ? (
        <Card>
          <Card.Body className="py-10 text-center">
            <div role="alert">
              <h2 className="text-lg font-semibold text-[var(--color-text)]">
                Unable to load dish intelligence
              </h2>

              <p className="mx-auto mt-2 max-w-md text-sm text-[var(--color-text-secondary)]">
                {error}
              </p>
            </div>

            <Button
              type="button"
              variant="secondary"
              className="mt-5"
              onClick={() => void refresh()}
            >
              Try again
            </Button>
          </Card.Body>
        </Card>
      ) : !hasSuggestedDishes ? (
        <Card>
          <Card.Body className="py-12 text-center">
            <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-[var(--color-surface-hover)]">
              <ChartPie
                size={22}
                className="text-[var(--color-text-secondary)]"
                aria-hidden="true"
              />
            </div>

            <h2 className="mt-4 text-lg font-semibold text-[var(--color-text)]">
              No AI suggestions available
            </h2>

            <p className="mx-auto mt-2 max-w-md text-sm text-[var(--color-text-secondary)]">
              Generate AI insights to see dish observations and recommendations
              based on customer feedback.
            </p>

            <Button
              type="button"
              variant="secondary"
              className="mt-5"
              onClick={() => void refresh()}
            >
              Refresh
            </Button>
          </Card.Body>
        </Card>
      ) : (
        <section className="space-y-5">
          <div className="flex flex-col gap-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4 sm:flex-row sm:items-center sm:justify-between sm:px-5">
            <div className="min-w-0">
              <h2 className="text-base font-semibold leading-6 text-[var(--color-text)]">
                Dish Intelligence
              </h2>

              <p className="mt-1 max-w-xl text-sm leading-5 text-[var(--color-text-secondary)]">
                Explore AI-generated observations and recommendations for each dish.
              </p>
            </div>

            <div className="flex w-full flex-col gap-1.5 sm:w-[35rem] sm:shrink-0">
              <label
                htmlFor="dish-intelligence-selector"
                className="text-xs font-semibold tracking-wide text-[var(--color-text-secondary)]"
              >
                 Select Dish
              </label>

              <select
                id="dish-intelligence-selector"
                value={selectedDish?.dishId ?? ""}
                onChange={(event) =>
                  setSelectedDishId(Number(event.target.value))
                }
                className="h-11 w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] px-3 text-sm font-medium text-[var(--color-text)] outline-none transition-colors focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20"
              >
                {suggestedDishes.map((dish) => (
                  <option
                    key={dish.dishId}
                    value={dish.dishId}
                  >
                    {dish.dishName}
                    {dish.active ? "" : " (Inactive)"}
                  </option>
                ))}
              </select>

              <p className="text-xs text-[var(--color-text-secondary)]">
                {suggestedDishes.length} dish
                {suggestedDishes.length === 1 ? "" : "es"} with AI suggestions
              </p>
            </div>
          </div>

          {view === "cards" ? (
            <DishIntelligenceCard dish={selectedDish} />
          ) : (
            <DishIntelligenceCharts dish={selectedDish} />
          )}
        </section>
      )}
    </section>
  );
}
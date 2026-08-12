import StatsCard from "@/components/common/ui/StatsCard";

import {
  UtensilsCrossed,
  Soup,
  Pizza,
  Wheat,
  CirclePlus,
} from "lucide-react";

import type {
  MealInsightsResponse,
} from "../types";

type MealInsightsProps = {
  meals: MealInsightsResponse;
};

export default function MealInsights({
  meals,
}: MealInsightsProps) {

  return (

    <div className="space-y-4">

      <div>

        <h2 className="text-lg font-semibold">
          Meal Insights
        </h2>

        <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
          Meal consumption summary for the selected month.
        </p>

      </div>

      <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">

        <StatsCard
          title="Total Meals"
          value={meals.totalMeals}
          description="Meals served"
          icon={<UtensilsCrossed size={26} />}
        />

        <StatsCard
          title="Full Meals"
          value={meals.fullMeals}
          description="Full meal count"
          icon={<Pizza size={26} />}
        />

        <StatsCard
          title="Half Meals"
          value={meals.halfMeals}
          description="Half meal count"
          icon={<Soup size={26} />}
        />

        <StatsCard
          title="Total Rotis"
          value={meals.totalRotis}
          description="Rotis served"
          icon={<Wheat size={26} />}
        />

        <StatsCard
          title="Extra Rotis"
          value={meals.extraRotis}
          description="Additional rotis"
          icon={<CirclePlus size={26} />}
        />

      </div>

    </div>

  );

}
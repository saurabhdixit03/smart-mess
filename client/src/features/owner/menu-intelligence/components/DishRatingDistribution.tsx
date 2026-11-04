
import {
  MessageSquareText,
  Star,
  Utensils,
} from "lucide-react";

import StatsCard from "@/components/common/ui/StatsCard";
import { Card } from "@/components/common/ui";

import type {
  DishRatingAnalytics,
  DishFeedbackAnalytics,
} from "../types";

type DishRatingDistributionProps = {
  dishRatings: DishRatingAnalytics[];
  dishFeedback: DishFeedbackAnalytics[];
};

const numberFormat = new Intl.NumberFormat("en-IN");

const ratingLevels = [
  { rating: 5, label: "5 stars" },
  { rating: 4, label: "4 stars" },
  { rating: 3, label: "3 stars" },
  { rating: 2, label: "2 stars" },
  { rating: 1, label: "1 star" },
] as const;

export default function DishRatingDistribution({
  dishRatings,
  dishFeedback,
}: DishRatingDistributionProps) {
  const totalRatings = dishRatings.reduce(
    (total, dish) => total + dish.totalRatings,
    0
  );

  const totalRatingPoints = dishRatings.reduce(
    (total, dish) =>
      total + dish.averageRating * dish.totalRatings,
    0
  );

  const averageRating =
    totalRatings > 0
      ? (totalRatingPoints / totalRatings).toFixed(1)
      : "—";

  const totalComments = dishFeedback.reduce(
    (total, dish) => total + dish.totalComments,
    0
  );

  const distribution = ratingLevels.map((level) => {
    const count = dishRatings.reduce((total, dish) => {
      const ratingCount = {
        1: dish.oneStarCount,
        2: dish.twoStarCount,
        3: dish.threeStarCount,
        4: dish.fourStarCount,
        5: dish.fiveStarCount,
      }[level.rating];

      return total + ratingCount;
    }, 0);

    return {
      ...level,
      count,
    };
  });

  return (
    <Card className="p-4 sm:p-5">
      <div className="mb-5">
        <h2 className="font-semibold text-[var(--color-text)]">
          Feedback Overview
        </h2>

        <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
          A summary of customer ratings and written feedback.
        </p>
      </div>

      <div className="mb-6 grid grid-cols-1 gap-3 sm:grid-cols-3">
        <StatsCard
          title="Total ratings"
          value={numberFormat.format(totalRatings)}
          description="Across all dishes"
          icon={<Star size={22} />}
        />

        <StatsCard
          title="Average rating"
          value={averageRating}
          description="Weighted average out of 5"
          icon={<Utensils size={22} />}
        />

        <StatsCard
          title="Dish comments"
          value={numberFormat.format(totalComments)}
          description="Written dish feedback"
          icon={<MessageSquareText size={22} />}
        />
      </div>

      <div className="border-t border-[var(--color-border)] pt-5">
        <div className="mb-4 flex items-center justify-between gap-2">
          <h3 className="text-sm font-semibold text-[var(--color-text)]">
            Rating Distribution
          </h3>

          <span className="text-xs text-[var(--color-text-secondary)]">
            {numberFormat.format(totalRatings)} ratings
          </span>
        </div>

        {totalRatings === 0 ? (
          <div className="flex min-h-32 items-center justify-center text-center">
            <p className="text-sm text-[var(--color-text-secondary)]">
              No ratings available to display.
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {distribution.map((item) => {
              const percentage =
                (item.count / totalRatings) * 100;

              return (
                <div
                  key={item.rating}
                  className="flex items-center gap-3"
                >
                  <span className="w-14 shrink-0 text-xs font-medium text-[var(--color-text-secondary)]">
                    {item.label}
                  </span>

                  <div className="h-3 flex-1 overflow-hidden rounded-full bg-[var(--color-surface-hover)]">
                    <div
                      className="h-full rounded-full bg-[var(--color-primary)] transition-all"
                      style={{ width: `${percentage}%` }}
                    />
                  </div>

                  <span className="w-10 shrink-0 text-right text-xs font-semibold text-[var(--color-text)]">
                    {numberFormat.format(item.count)}
                  </span>

                  <span className="w-12 shrink-0 text-right text-xs text-[var(--color-text-secondary)]">
                    {percentage.toFixed(1)}%
                  </span>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </Card>
  );
}

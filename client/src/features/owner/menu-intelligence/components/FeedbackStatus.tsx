
import {
  MessageSquareText,
  Star,
  Utensils,
} from "lucide-react";

import StatsCard from "@/components/common/ui/StatsCard";

import type { DishIntelligence } from "../types";

type FeedbackStatusProps = {
  dishes: DishIntelligence[];
};

const numberFormat = new Intl.NumberFormat("en-IN");

export default function FeedbackStatus({
  dishes,
}: FeedbackStatusProps) {
  const totalRatings = dishes.reduce(
    (total, dish) => total + dish.totalRatings,
    0
  );

  const totalRatingPoints = dishes.reduce(
    (total, dish) =>
      total +
      (dish.averageRating ?? 0) * dish.totalRatings,
    0
  );

  const averageRating =
    totalRatings > 0
      ? (totalRatingPoints / totalRatings).toFixed(1)
      : "—";

  const totalComments = dishes.reduce(
    (total, dish) => total + dish.totalComments,
    0
  );

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
      <StatsCard
        title="Total dish ratings"
        value={numberFormat.format(totalRatings)}
        description="Ratings received across all dishes"
        icon={<Star size={24} />}
      />

      <StatsCard
        title="Average dish rating"
        value={averageRating}
        description="Weighted average out of 5"
        icon={<Utensils size={24} />}
      />

      <StatsCard
        title="Dish comments"
        value={numberFormat.format(totalComments)}
        description="Written comments linked to dishes"
        icon={<MessageSquareText size={24} />}
      />
    </div>
  );
}

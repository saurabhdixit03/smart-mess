import {
  ClipboardList,
  UtensilsCrossed,
  Soup,
} from "lucide-react";

import StatsCard from "@/components/common/ui/StatsCard/StatsCard";

type MealRecordSummaryProps = {
  pendingMeals: number;
  fullMeals: number;
  halfMeals: number;
};

export default function MealRecordSummary({
  pendingMeals,
  fullMeals,
  halfMeals,
}: MealRecordSummaryProps) {
  return (

    <div className="grid grid-cols-1 gap-4 md:grid-cols-3">

      <StatsCard
  title="Pending Meals"
  value={pendingMeals}
  description="Awaiting collection"
  icon={<ClipboardList size={28} />}
/>

<StatsCard
  title="Full Meals"
  value={fullMeals}
  description="Remaining full meals"
  icon={<UtensilsCrossed size={28} />}
/>

<StatsCard
  title="Half Meals"
  value={halfMeals}
  description="Remaining half meals"
  icon={<Soup size={28} />}
/>

    </div>

  );
}
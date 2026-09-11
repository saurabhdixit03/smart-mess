import { useState } from "react";

import MealMenuCard from "../MealMenuCard";
import PublishMenuDialog from "../PublishMenuDialog";

import type {
  MenuAvailabilityResponse,
  MenuResponse,
} from "../../types/menu.types";

type TodayMenusProps = {
  todayMenus: MenuResponse[];
  availability: MenuAvailabilityResponse[];
  loading: boolean;
  error: string | null;
  onRefresh: () => Promise<void>;
};

export default function TodayMenus({
  todayMenus,
  availability,
  loading,
  error,
  onRefresh,
}: TodayMenusProps) {
  const [openDialog, setOpenDialog] = useState(false);

  const [selectedMeal, setSelectedMeal] =
    useState<"Lunch" | "Dinner">("Lunch");

  const lunchMenu = todayMenus.find(
    (menu) => menu.mealSession === "LUNCH"
  );

  const dinnerMenu = todayMenus.find(
    (menu) => menu.mealSession === "DINNER"
  );

  const lunchAvailability = availability.find(
    (item) => item.mealSession === "LUNCH"
  );

  const dinnerAvailability = availability.find(
    (item) => item.mealSession === "DINNER"
  );

  if (loading) {
    return (
      <div className="py-8 text-center">
        Loading today's menus...
      </div>
    );
  }

  if (error) {
    return (
      <div className="py-8 text-center text-red-500">
        {error}
      </div>
    );
  }

  return (
    <>
      <section className="space-y-6">
        <div className="grid gap-6 lg:grid-cols-2">

          <MealMenuCard
            title="Lunch"
            menu={lunchMenu}
            availability={lunchAvailability}
            onPublish={() => {
              setSelectedMeal("Lunch");
              setOpenDialog(true);
            }}
          />

          <MealMenuCard
            title="Dinner"
            menu={dinnerMenu}
            availability={dinnerAvailability}
            onPublish={() => {
              setSelectedMeal("Dinner");
              setOpenDialog(true);
            }}
          />

        </div>
      </section>

      <PublishMenuDialog
        open={openDialog}
        title={`Publish ${selectedMeal} Menu`}
        mealSession={
          selectedMeal === "Lunch"
            ? "LUNCH"
            : "DINNER"
        }
        onSuccess={onRefresh}
        onClose={() => setOpenDialog(false)}
      />
    </>
  );
}
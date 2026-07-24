import { useState } from "react";

import MealMenuCard from "../MealMenuCard";
import PublishMenuDialog from "../PublishMenuDialog";

import type { MenuResponse } from "../../types/menu.types";

type TodayMenusProps = {
  todayMenus: MenuResponse[];
  loading: boolean;
  error: string | null;
  onRefresh: () => Promise<void>;
};

export default function TodayMenus({
  todayMenus,
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
            onPublish={() => {
              setSelectedMeal("Lunch");
              setOpenDialog(true);
            }}
          />

          <MealMenuCard
            title="Dinner"
            menu={dinnerMenu}
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
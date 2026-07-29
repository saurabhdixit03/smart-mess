import Card from "@/components/common/ui/Card/Card";
import StatusBadge from "@/components/common/ui/StatusBadge";

import type { MenuResponse } from "@/features/menu/types/menu.types";

type MealSessionSelectorProps = {
  menus: MenuResponse[];
  selectedSession: "LUNCH" | "DINNER";
  onSelect: (
    session: "LUNCH" | "DINNER"
  ) => void;

  title?: string;
  variant?: "cards" | "toggle";
};

export default function MealSessionSelector({
  menus,
  selectedSession,
  onSelect,
  title = "Live Dashboard",
  variant = "cards",
}: MealSessionSelectorProps) {

  const orderedMenus = [...menus].sort((a, b) => {
    if (a.mealSession === b.mealSession) {
      return 0;
    }

    return a.mealSession === "LUNCH"
      ? -1
      : 1;
  });

  if (variant === "toggle") {

    return (

      <div className="flex items-center justify-between">

        <h2 className="text-lg font-semibold">
          {title}
        </h2>

        <div className="inline-flex rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] p-1">

          {orderedMenus.map((menu) => {

            const selected =
              menu.mealSession === selectedSession;

            return (

              <button
                key={menu.menuId}
                type="button"
                onClick={() =>
                  onSelect(menu.mealSession)
                }
                className={`
                  rounded-md px-5 py-2 text-sm font-medium transition-all duration-200
                  ${
                    selected
                      ? "bg-[var(--color-primary)] text-white shadow-sm"
                      : "text-[var(--color-text-secondary)] hover:bg-[var(--color-background)]"
                  }
                `}
              >
                {menu.mealSession === "LUNCH"
                  ? "Lunch"
                  : "Dinner"}
              </button>

            );

          })}

        </div>

      </div>

    );

  }

  return (

    <div className="space-y-3">

      <h2 className="text-lg font-semibold">
        {title}
      </h2>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">

        {orderedMenus.map((menu) => {

          const selected =
            menu.mealSession === selectedSession;

          return (

            <Card
              key={menu.menuId}
              onClick={() =>
                onSelect(menu.mealSession)
              }
              className={`
                cursor-pointer
                border
                transition-all
                duration-300
                ease-out
                hover:-translate-y-1
                hover:shadow-xl
                hover:border-[var(--color-primary)]
                hover:scale-[1.01]
                active:translate-y-0
                active:scale-100
                ${
                  selected
                    ? "border-[var(--color-primary)]"
                    : "border-[var(--color-border)]"
                }
              `}
            >

              <Card.Header className="flex items-center justify-between py-4">

                <div>

                  <h3 className="text-lg font-semibold">

                    {menu.mealSession === "LUNCH"
                      ? "Lunch"
                      : "Dinner"}

                  </h3>

                  <p className="mt-0.5 text-sm text-[var(--color-text-secondary)]">

                    {menu.menuDate}

                  </p>

                </div>

                <StatusBadge
                  label={
                    selected
                      ? "Live"
                      : "Available"
                  }
                  variant={
                    menu.mealSession === "LUNCH"
                      ? "lunch"
                      : "dinner"
                  }
                />

              </Card.Header>

              <Card.Body className="py-4">

                <div className="flex flex-wrap items-center gap-x-2 gap-y-2 text-sm">

                  <span className="rounded-full bg-green-100 px-2.5 py-1 text-xs font-medium text-green-700">
                    {menu.sabjiOne}
                  </span>

                  <span className="rounded-full bg-green-100 px-2.5 py-1 text-xs font-medium text-green-700">
                    {menu.sabjiTwo}
                  </span>

                  <span className="text-[var(--color-text-secondary)]">
                    {menu.dal}
                  </span>

                  <span className="text-[var(--color-text-secondary)]">
                    •
                  </span>

                  <span className="text-[var(--color-text-secondary)]">
                    {menu.rice}
                  </span>

                  <span className="text-[var(--color-text-secondary)]">
                    •
                  </span>

                  <span className="text-[var(--color-text-secondary)]">
                    {menu.sweet}
                  </span>

                </div>

              </Card.Body>

            </Card>

          );

        })}

      </div>

    </div>

  );

}
import Card from "@/components/common/ui/Card/Card";
import StatusBadge from "@/components/common/ui/StatusBadge";

import type { MenuResponse } from "@/features/owner/menu/types/menu.types";

type DashboardMenuCardProps = {
  menu: MenuResponse | undefined;
};

export default function DashboardMenuCard({
  menu,
}: DashboardMenuCardProps) {
  if (!menu) {
    return (
      <Card>
        <Card.Body className="py-12 text-center text-[var(--color-text-secondary)]">
          No menu published for this session.
        </Card.Body>
      </Card>
    );
  }

  return (
    <Card className="interactive-surface">

      <Card.Header className="flex items-center justify-between">

        <div>
          <h3 className="text-lg font-semibold">
            Today's Menu
          </h3>

          <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
            {menu.menuDate}
          </p>
        </div>

        <StatusBadge
          label={
            menu.mealSession === "LUNCH"
              ? "Lunch"
              : "Dinner"
          }
          variant={
            menu.mealSession === "LUNCH"
              ? "lunch"
              : "dinner"
          }
        />

      </Card.Header>

      <Card.Body>

        <div className="grid gap-3 md:grid-cols-2">

          <div>

            <p className="text-xs uppercase tracking-wide text-[var(--color-text-secondary)]">
              Sabji 1
            </p>

            <p className="font-medium">
              {menu.sabjiOne}
            </p>

          </div>

          <div>

            <p className="text-xs uppercase tracking-wide text-[var(--color-text-secondary)]">
              Sabji 2
            </p>

            <p className="font-medium">
              {menu.sabjiTwo}
            </p>

          </div>

          <div>

            <p className="text-xs uppercase tracking-wide text-[var(--color-text-secondary)]">
              Dal
            </p>

            <p className="font-medium">
              {menu.dal}
            </p>

          </div>

          <div>

            <p className="text-xs uppercase tracking-wide text-[var(--color-text-secondary)]">
              Rice
            </p>

            <p className="font-medium">
              {menu.rice}
            </p>

          </div>

          <div>

            <p className="text-xs uppercase tracking-wide text-[var(--color-text-secondary)]">
              Sweet
            </p>

            <p className="font-medium">
              {menu.sweet}
            </p>

          </div>

        </div>

      </Card.Body>

    </Card>
  );
}
import { Button, Card } from "@/components/common/ui";

import { MenuCard } from ".";

import type { Menu } from "../types";

interface TodayMenusProps {
  todayMenus: Menu[];
  loading: boolean;
  error: string | null;
  onRefresh: () => void;
}

export default function TodayMenus({
  todayMenus,
  loading,
  error,
  onRefresh,
}: TodayMenusProps) {
  if (loading) {
    return (
      <Card>
        <Card.Body className="py-10 text-center text-[var(--color-text-secondary)]">
          Loading today's menu...
        </Card.Body>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <Card.Body className="space-y-4 text-center">
          <p className="text-[var(--color-danger)]">
            {error}
          </p>

          <Button
            onClick={onRefresh}
            variant="primary"
          >
            Retry
          </Button>
        </Card.Body>
      </Card>
    );
  }

  if (todayMenus.length === 0) {
    return (
      <Card>
        <Card.Body className="py-10 text-center text-[var(--color-text-secondary)]">
          No menu has been published for today.
        </Card.Body>
      </Card>
    );
  }

  const lunchMenu = todayMenus.find(
    (menu) => menu.mealSession === "LUNCH"
  );

  const dinnerMenu = todayMenus.find(
    (menu) => menu.mealSession === "DINNER"
  );

  return (
  <div className="grid gap-6 lg:grid-cols-2">
    {lunchMenu && (
      <MenuCard menu={lunchMenu} />
    )}

    {dinnerMenu && (
      <MenuCard menu={dinnerMenu} />
    )}
  </div>
);
}
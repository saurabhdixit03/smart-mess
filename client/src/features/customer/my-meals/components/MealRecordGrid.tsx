import { Button, Card } from "@/components/common/ui";

import MealRecordCard from "./MealRecordCard";

import type { MealRecord } from "../types";

interface MealRecordGridProps {
  mealRecords: MealRecord[];
  loading: boolean;
  error: string | null;
  onRefresh: () => void;
}

export default function MealRecordGrid({
  mealRecords,
  loading,
  error,
  onRefresh,
}: MealRecordGridProps) {
  if (loading) {
    return (
      <Card>
        <Card.Body className="py-10 text-center text-[var(--color-text-secondary)]">
          Loading meal history...
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
            variant="primary"
            onClick={onRefresh}
          >
            Retry
          </Button>

        </Card.Body>
      </Card>
    );
  }

  if (mealRecords.length === 0) {
    return (
      <Card>
        <Card.Body className="py-10 text-center text-[var(--color-text-secondary)]">
          You haven't collected any meals yet.
        </Card.Body>
      </Card>
    );
  }

  return (
    <div
      className="
        grid
        gap-5

        grid-cols-1
        md:grid-cols-2
        xl:grid-cols-4
      "
    >
      {mealRecords.map((mealRecord) => (
        <MealRecordCard
          key={mealRecord.mealRecordId}
          mealRecord={mealRecord}
        />
      ))}
    </div>
  );
}
import type { ReactNode } from "react";

import Card from "../Card/Card";

type StatsCardProps = {
  title: string;
  value: string | number;
  description?: string;
  icon?: ReactNode;
};

export default function StatsCard({
  title,
  value,
  description,
  icon,
}: StatsCardProps) {
  return (
    <Card className="interactive-surface p-6">
      <div className="flex items-start justify-between">

        <div className="space-y-1">

          <p className="text-sm font-medium text-[var(--color-text-secondary)]">
            {title}
          </p>

          <h2 className="text-3xl font-bold tracking-tight text-[var(--color-text)]">
            {value}
          </h2>

          {description && (
            <p className="text-sm text-[var(--color-text-secondary)]">
              {description}
            </p>
          )}

        </div>

        {icon && (
          <div
            className="
              flex h-14 w-14 items-center justify-center
              rounded-xl
              bg-[var(--color-primary)]/10
              text-[var(--color-primary)]
            "
          >
            {icon}
          </div>
        )}

      </div>
    </Card>
  );
}
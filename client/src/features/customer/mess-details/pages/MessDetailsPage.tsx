import {
  CalendarDays,
  Clock3,
  IndianRupee,
} from "lucide-react";

import type { ReactNode } from "react";

import Card from "@/components/common/ui/Card/Card";
import PageHeader from "@/components/common/ui/PageHeader";

import { useMessDetails } from "../hooks/useMessDetails";

const currencyFormat = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

function formatTime(value: string | null): string {
  if (!value) {
    return "Not configured";
  }

  const [hours, minutes] = value.split(":").map(Number);

  if (
    !Number.isInteger(hours) ||
    !Number.isInteger(minutes) ||
    hours < 0 ||
    hours > 23 ||
    minutes < 0 ||
    minutes > 59
  ) {
    return "Not configured";
  }

  const date = new Date();
  date.setHours(hours, minutes, 0, 0);

  return date.toLocaleTimeString("en-IN", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  });
}

function formatDay(value: string | null): string {
  if (!value) {
    return "No weekly off";
  }

  return value.charAt(0).toUpperCase() +
    value.slice(1).toLowerCase();
}

type DetailRowProps = {
  label: string;
  value: string;
};

function DetailRow({
  label,
  value,
}: DetailRowProps) {
  return (
    <div className="flex items-start justify-between gap-4">
      <dt className="text-sm leading-5 text-[var(--color-text-secondary)]">
        {label}
      </dt>

      <dd className="min-w-0 break-words text-right text-sm font-medium leading-5 tabular-nums text-[var(--color-text)]">
        {value}
      </dd>
    </div>
  );
}

type DetailCardProps = {
  icon: ReactNode;
  title: string;
  description: string;
  children: ReactNode;
};

function DetailCard({
  icon,
  title,
  description,
  children,
}: DetailCardProps) {
  return (
    <Card className="interactive-surface h-full">
      <Card.Body className="p-4">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[var(--color-primary)]/10 text-[var(--color-primary)]">
            {icon}
          </div>

          <h2 className="text-base font-semibold tracking-tight text-[var(--color-text)]">
            {title}
          </h2>
        </div>

        <p className="mt-2 text-xs leading-5 text-[var(--color-text-secondary)]">
          {description}
        </p>

        <dl className="mt-3 space-y-3 rounded-xl border border-[var(--color-border)] bg-[#FAFAF8] p-3">
          {children}
        </dl>
      </Card.Body>
    </Card>
  );
}

export default function MessDetailsPage() {
  const {
    settings,
    pricing,
    loading,
    error,
  } = useMessDetails();

  const closedSessions: string[] = [];

  if (settings?.weeklyLunchClosed) {
    closedSessions.push("Lunch");
  }

  if (settings?.weeklyDinnerClosed) {
    closedSessions.push("Dinner");
  }

  const weeklySessionSummary =
    settings?.weeklyClosedDay && closedSessions.length > 0
      ? closedSessions.join(" & ")
      : "None";

  return (
    <section className="min-w-0 space-y-4">
      <PageHeader
        title="Mess Details"
        description="View response deadlines, weekly schedule, and current meal prices."
      />

      {loading ? (
        <Card>
          <Card.Body className="py-10 text-center">
            <p
              role="status"
              className="text-sm text-[var(--color-text-secondary)]"
            >
              Loading mess details...
            </p>
          </Card.Body>
        </Card>
      ) : error ? (
        <Card>
          <Card.Body className="py-8 text-center">
            <p
              role="alert"
              className="text-sm text-[var(--color-danger)]"
            >
              {error}
            </p>
          </Card.Body>
        </Card>
      ) : (
        <div className="grid items-stretch gap-4 md:grid-cols-2 xl:grid-cols-3">
          <DetailCard
            icon={<Clock3 size={20} aria-hidden="true" />}
            title="Response Window"
            description="Submit your response before these deadlines."
          >
            <DetailRow
              label="Lunch"
              value={formatTime(
                settings?.lunchResponseCutoff ?? null
              )}
            />

            <DetailRow
              label="Dinner"
              value={formatTime(
                settings?.dinnerResponseCutoff ?? null
              )}
            />
          </DetailCard>

          <DetailCard
            icon={
              <CalendarDays size={20} aria-hidden="true" />
            }
            title="Weekly Schedule"
            description="Regular weekly closures for your mess."
          >
            <DetailRow
              label="Closed day"
              value={formatDay(
                settings?.weeklyClosedDay ?? null
              )}
            />

            <DetailRow
              label="Closed meals"
              value={weeklySessionSummary}
            />
          </DetailCard>

          <DetailCard
            icon={
              <IndianRupee size={20} aria-hidden="true" />
            }
            title="Meal Pricing"
            description="Current prices applied when meals are collected."
          >
            <DetailRow
              label="Full meal"
              value={
                pricing
                  ? currencyFormat.format(
                      pricing.fullMealPrice
                    )
                  : "Not available"
              }
            />

            <DetailRow
              label="Half meal"
              value={
                pricing
                  ? currencyFormat.format(
                      pricing.halfMealPrice
                    )
                  : "Not available"
              }
            />

            <DetailRow
              label="Extra roti"
              value={
                pricing
                  ? currencyFormat.format(
                      pricing.extraRotiPrice
                    )
                  : "Not available"
              }
            />
          </DetailCard>
        </div>
      )}
    </section>
  );
}
import {
  CalendarDays,
  Clock3,
  IndianRupee,
} from "lucide-react";

import type { ReactNode } from "react";

import PageHeader from "@/components/common/ui/PageHeader";

import { useMessDetails } from "../hooks/useMessDetails";

function formatTime(
  value: string | null
) {
  if (!value) {
    return "Not configured";
  }

  const [hours, minutes] =
    value.split(":");

  const date = new Date();

  date.setHours(
    Number(hours),
    Number(minutes),
    0,
    0
  );

  return date.toLocaleTimeString(
    "en-IN",
    {
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    }
  );
}

function formatDay(
  value: string | null
) {
  if (!value) {
    return "No weekly off";
  }

  return (
    value.charAt(0) +
    value
      .slice(1)
      .toLowerCase()
  );
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
    <div
      className="
        flex
        items-center
        justify-between
        gap-4
      "
    >
      <span
        className="
          text-sm
          text-[var(--color-text-secondary)]
        "
      >
        {label}
      </span>

      <span
        className="
          text-right
          text-sm
          font-semibold
          text-[var(--color-text)]
        "
      >
        {value}
      </span>
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
    <section
      className="
        flex
        min-h-[210px]
        flex-col
        rounded-xl
        border
        border-[var(--color-border)]
        bg-[var(--color-surface)]
        p-5
        shadow-sm
        transition-all
        duration-200
        hover:-translate-y-1
        hover:shadow-md
      "
    >
      <div className="flex items-start gap-3">

        <div
          className="
            flex
            h-9
            w-9
            shrink-0
            items-center
            justify-center
            rounded-lg
            bg-[var(--color-primary)]/10
            text-[var(--color-primary)]
          "
        >
          {icon}
        </div>

        <div>

          <h2
            className="
              text-base
              font-semibold
              text-[var(--color-text)]
            "
          >
            {title}
          </h2>

          <p
            className="
              mt-1
              text-xs
              leading-5
              text-[var(--color-text-secondary)]
            "
          >
            {description}
          </p>

        </div>

      </div>

      <div
        className="
          mt-auto
          space-y-3
          pt-6
        "
      >
        {children}
      </div>

    </section>
  );
}

export default function MessDetailsPage() {
  const {
    settings,
    pricing,
    loading,
    error,
  } = useMessDetails();

  if (loading) {
    return (
      <div className="p-6">
        Loading mess details...
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6">

        <p className="text-sm text-[var(--color-danger)]">
          {error}
        </p>

      </div>
    );
  }

  const closedSessions: string[] = [];

  if (settings?.weeklyLunchClosed) {
    closedSessions.push("Lunch");
  }

  if (settings?.weeklyDinnerClosed) {
    closedSessions.push("Dinner");
  }

  const weeklySessionSummary =
    closedSessions.length > 0
      ? closedSessions.join(" & ")
      : "None";

  return (
    <div className="space-y-5">

      <PageHeader
        title="Mess Details"
        description="View meal response timings, weekly schedule, and current meal pricing."
      />

      <div
        className="
          grid
          gap-4
          md:grid-cols-2
          xl:grid-cols-3
        "
      >

        <DetailCard
          icon={<Clock3 size={18} />}
          title="Response Window"
          description="Meal response deadlines for lunch and dinner."
        >
          <DetailRow
            label="Lunch"
            value={formatTime(
              settings?.lunchResponseCutoff ??
                null
            )}
          />

          <DetailRow
            label="Dinner"
            value={formatTime(
              settings?.dinnerResponseCutoff ??
                null
            )}
          />
        </DetailCard>

        <DetailCard
          icon={<CalendarDays size={18} />}
          title="Weekly Schedule"
          description="Recurring weekly mess closure details."
        >
          <DetailRow
            label="Closed Day"
            value={formatDay(
              settings?.weeklyClosedDay ??
                null
            )}
          />

          <DetailRow
            label="Closed Meals"
            value={weeklySessionSummary}
          />
        </DetailCard>

        <DetailCard
          icon={<IndianRupee size={18} />}
          title="Meal Pricing"
          description="Current meal prices used for billing."
        >
          <DetailRow
            label="Half Meal"
            value={
              pricing
                ? `₹${pricing.halfMealPrice}`
                : "Not available"
            }
          />

          <DetailRow
            label="Full Meal"
            value={
              pricing
                ? `₹${pricing.fullMealPrice}`
                : "Not available"
            }
          />

          <DetailRow
            label="Extra Roti"
            value={
              pricing
                ? `₹${pricing.extraRotiPrice}`
                : "Not available"
            }
          />
        </DetailCard>

      </div>

    </div>
  );
}
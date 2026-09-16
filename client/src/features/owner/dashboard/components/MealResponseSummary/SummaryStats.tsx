import RollingCounter from "@/components/common/business/RollingCounter";

import type {
  DashboardSummary,
} from "../../types/dashboard.types";

type SummaryStatsProps = {
  dashboard: DashboardSummary;
};

export default function SummaryStats({
  dashboard,
}: SummaryStatsProps) {
  const responseStats = [
    {
      label: "Accepted",
      value:
        dashboard.acceptedResponses,
      className:
        "border-[var(--color-primary)]/20 bg-[var(--color-primary)]/10 text-[var(--color-primary)]",
    },
    {
      label: "Pending",
      value:
        dashboard.pendingResponses,
      className:
        "border-amber-200 bg-amber-50 text-amber-700",
    },
    {
      label: "Declined",
      value:
        dashboard.declinedResponses,
      className:
        "border-[var(--color-danger)]/20 bg-[var(--color-danger)]/10 text-[var(--color-danger)]",
    },
  ];

  const preparationStats = [
    {
      label: "Full",
      value:
        dashboard.expectedFullMeals,
    },
    {
      label: "Half",
      value:
        dashboard.expectedHalfMeals,
    },
    {
      label: "Base Rotis",
      value:
        dashboard.baseRotisRequired,
    },
    {
      label: "Extra Rotis",
      value:
        dashboard.expectedExtraRotis,
    },
    {
      label: "Total Rotis",
      value:
        dashboard.totalRotisRequired,
    },
  ];

  return (
    <div className="space-y-4">

      {/* Response metrics */}
      <section className="space-y-2">

        <h3
          className="
            text-xs
            font-semibold
            uppercase
            tracking-wide
            text-[var(--color-text-secondary)]
          "
        >
          Customer Responses
        </h3>

        <div className="grid grid-cols-3 gap-3">

          {responseStats.map(
            ({
              label,
              value,
              className,
            }) => (
              <div
                key={label}
                className={`
                  min-w-0
                  rounded-xl
                  border
                  px-3
                  py-3
                  text-center
                  ${className}
                `}
              >
                <p className="text-2xl font-bold leading-none">
                  <RollingCounter
                    value={value}
                  />
                </p>

                <p className="mt-1.5 truncate text-xs font-medium">
                  {label}
                </p>
              </div>
            )
          )}

        </div>

      </section>

      {/* Kitchen preparation metrics */}
      <section className="space-y-2">

        <h3
          className="
            text-xs
            font-semibold
            uppercase
            tracking-wide
            text-[var(--color-text-secondary)]
          "
        >
          Kitchen Preparation
        </h3>

        <div
          className="
            grid
            grid-cols-2
            gap-3
            sm:grid-cols-3
            xl:grid-cols-5
          "
        >
          {preparationStats.map(
            ({
              label,
              value,
            }) => (
              <div
                key={label}
                className="
                  min-w-0
                  rounded-xl
                  border
                  border-[var(--color-primary)]/15
                  bg-[var(--color-primary)]/5
                  px-3
                  py-3
                  text-center
                  text-[var(--color-primary)]
                "
              >
                <p className="text-xl font-semibold leading-none">
                  <RollingCounter
                    value={value}
                  />
                </p>

                <p className="mt-1.5 truncate text-[11px] font-medium">
                  {label}
                </p>
              </div>
            )
          )}

        </div>

      </section>

    </div>
  );
}
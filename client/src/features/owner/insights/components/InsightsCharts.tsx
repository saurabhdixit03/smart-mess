import { useId } from "react";

import { Card } from "@/components/common/ui";

import type { MonthlyInsightsResponse } from "../types";

type ChartItem = {
  label: string;
  value: number;
  color: string;
};

const currencyFormat = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 2,
});

const countFormat = new Intl.NumberFormat("en-IN");

const colors = {
  primary: "var(--color-primary)",
  green: "#16a34a",
  amber: "#d97706",
};

function DoughnutChart({
  title,
  description,
  items,
  formatValue,
  totalLabel,
}: {
  title: string;
  description: string;
  items: ChartItem[];
  formatValue: (value: number) => string;
  totalLabel: string;
}) {
  const titleId = useId();
  const total = items.reduce((sum, item) => sum + item.value, 0);
  const radius = 70;
  const circumference = 2 * Math.PI * radius;

  const segments = items.map((item, index) => {
    const previousTotal = items
      .slice(0, index)
      .reduce((sum, previous) => sum + previous.value, 0);

    return {
      ...item,
      length: total > 0
        ? (item.value / total) * circumference
        : 0,
      offset: total > 0
        ? (previousTotal / total) * circumference
        : 0,
    };
  });

  return (
    <Card className="min-w-0 p-4 sm:p-5">
      <h2
        id={titleId}
        className="font-semibold text-[var(--color-text)]"
      >
        {title}
      </h2>

      <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
        {description}
      </p>

      {total > 0 ? (
        <div className="relative mx-auto my-5 h-52 w-52">
          <svg
            viewBox="0 0 200 200"
            className="h-full w-full"
            role="img"
            aria-labelledby={titleId}
          >
            <title>
              {items
                .map((item) =>
                  `${item.label}: ${formatValue(item.value)}`
                )
                .join(", ")}
            </title>

            <circle
              cx="100"
              cy="100"
              r={radius}
              fill="none"
              stroke="var(--color-surface-hover)"
              strokeWidth="22"
            />

            {segments
              .filter((item) => item.value > 0)
              .map((item) => (
                <circle
                  key={item.label}
                  cx="100"
                  cy="100"
                  r={radius}
                  fill="none"
                  stroke={item.color}
                  strokeWidth="22"
                  strokeDasharray={
                    `${item.length} ${circumference}`
                  }
                  strokeDashoffset={-item.offset}
                  transform="rotate(-90 100 100)"
                />
              ))}
          </svg>

          <div
            aria-hidden="true"
            className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center px-10 text-center"
          >
            <p className="text-xs text-[var(--color-text-secondary)]">
              {totalLabel}
            </p>

            <p className="mt-1 max-w-full break-words text-lg font-bold text-[var(--color-text)]">
              {formatValue(total)}
            </p>
          </div>
        </div>
      ) : (
        <p className="flex min-h-52 items-center justify-center text-sm text-[var(--color-text-secondary)]">
          No data for this period.
        </p>
      )}

      <dl className="space-y-3">
        {items.map((item) => (
          <div
            key={item.label}
            className="flex items-center justify-between gap-3 text-sm"
          >
            <dt className="flex items-center gap-2 text-[var(--color-text-secondary)]">
              <span
                aria-hidden="true"
                className="h-2.5 w-2.5 shrink-0 rounded-full"
                style={{ backgroundColor: item.color }}
              />
              {item.label}
            </dt>

            <dd className="text-right font-semibold text-[var(--color-text)]">
              {formatValue(item.value)}
              {total > 0 && (
                <span className="ml-2 text-xs font-normal text-[var(--color-text-secondary)]">
                  ({((item.value / total) * 100).toFixed(1)}%)
                </span>
              )}
            </dd>
          </div>
        ))}
      </dl>
    </Card>
  );
}

function BillStatusChart({
  financial,
}: {
  financial: MonthlyInsightsResponse["financial"];
}) {
  const items: ChartItem[] = [
    {
      label: "Paid bills",
      value: financial.paidBills,
      color: colors.green,
    },
    {
      label: "Unpaid bills",
      value: financial.pendingBills,
      color: colors.amber,
    },
  ];

  const maximum = Math.max(...items.map((item) => item.value));

  return (
    <Card className="min-w-0 p-4 sm:p-5">
      <h2 className="font-semibold text-[var(--color-text)]">
        Bill status
      </h2>

      <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
        Number of bills by payment status.
      </p>

      <p className="mt-5 text-2xl font-bold text-[var(--color-text)]">
        {countFormat.format(financial.billsGenerated)}
        <span className="ml-2 text-xs font-normal text-[var(--color-text-secondary)]">
          bills generated
        </span>
      </p>

      <dl className="mt-6 space-y-6">
        {items.map((item) => (
          <div key={item.label}>
            <div className="mb-2 flex items-center justify-between gap-3 text-sm">
              <dt className="text-[var(--color-text-secondary)]">
                {item.label}
              </dt>

              <dd className="font-semibold text-[var(--color-text)]">
                {countFormat.format(item.value)}
              </dd>
            </div>

            <div
              aria-hidden="true"
              className="h-4 overflow-hidden rounded-full bg-[var(--color-surface-hover)]"
            >
              <div
                className="h-full rounded-full"
                style={{
                  width: maximum > 0
                    ? `${(item.value / maximum) * 100}%`
                    : "0%",
                  backgroundColor: item.color,
                }}
              />
            </div>
          </div>
        ))}
      </dl>

      {financial.billsGenerated === 0 && (
        <p className="mt-6 text-sm text-[var(--color-text-secondary)]">
          No bills generated for this period.
        </p>
      )}
    </Card>
  );
}

export default function InsightsCharts({
  insights,
}: {
  insights: MonthlyInsightsResponse;
}) {
  const { financial, meals, customers } = insights;

  return (
    <div className="space-y-4">
      <div className="grid items-start gap-4 lg:grid-cols-3">
        <DoughnutChart
          title="Payment collection"
          description="Collected and outstanding billed amounts."
          totalLabel="Total shown"
          formatValue={(value) => currencyFormat.format(value)}
          items={[
            {
              label: "Collected",
              value: financial.collectedRevenue,
              color: colors.green,
            },
            {
              label: "Outstanding",
              value: financial.pendingRevenue,
              color: colors.amber,
            },
          ]}
        />

        <BillStatusChart financial={financial} />

        <DoughnutChart
          title="Meal portions"
          description="Full and half portions in billed meal records."
          totalLabel="Meals shown"
          formatValue={(value) => countFormat.format(value)}
          items={[
            {
              label: "Full meals",
              value: meals.fullMeals,
              color: colors.primary,
            },
            {
              label: "Half meals",
              value: meals.halfMeals,
              color: colors.amber,
            },
          ]}
        />
      </div>

      <Card className="p-4">
        <dl className="grid grid-cols-2 gap-4 lg:grid-cols-4">
          {[
            {
              label: "Collection rate",
              value: `${financial.collectionRate.toLocaleString(
                "en-IN",
                { maximumFractionDigits: 1 }
              )}%`,
            },
            {
              label: "Billed customers",
              value: countFormat.format(customers.activeCustomers),
            },
            {
              label: "Total rotis",
              value: countFormat.format(meals.totalRotis),
            },
            {
              label: "Extra rotis",
              value: countFormat.format(meals.extraRotis),
            },
          ].map((item) => (
            <div key={item.label}>
              <dt className="text-xs text-[var(--color-text-secondary)]">
                {item.label}
              </dt>
              <dd className="mt-1 text-xl font-semibold text-[var(--color-text)]">
                {item.value}
              </dd>
            </div>
          ))}
        </dl>
      </Card>

      <p className="text-xs leading-5 text-[var(--color-text-secondary)]">
        Figures reflect the selected billing period.
        Meal totals include billed collections.
      </p>
    </div>
  );
}
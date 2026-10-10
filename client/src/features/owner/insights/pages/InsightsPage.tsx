import { useState, type ReactNode } from "react";

import {
  ChartPie,
  CheckCircle2,
  CirclePlus,
  Clock3,
  IndianRupee,
  LayoutGrid,
  Percent,
  Receipt,
  Soup,
  Users,
  UtensilsCrossed,
  Wallet,
  Wheat,
} from "lucide-react";

import {
  Button,
  Card,
  PageHeader,
} from "@/components/common/ui";

import { InsightsFilters } from "../components";
import InsightsCharts from "../components/InsightsCharts";
import { useInsights } from "../hooks";

type InsightsView = "cards" | "charts";

const currencyFormat = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

const countFormat = new Intl.NumberFormat("en-IN");

const summaryGrid =
  "grid grid-cols-2 gap-3 lg:grid-cols-4";

const mealGrid =
  "grid grid-cols-2 gap-3 lg:grid-cols-3 xl:grid-cols-5";

function getCurrentPeriod() {
  const parts = new Intl.DateTimeFormat("en", {
    timeZone: "Asia/Kolkata",
    month: "numeric",
    year: "numeric",
  }).formatToParts(new Date());

  return {
    month: Number(
      parts.find((part) => part.type === "month")!.value
    ),
    year: Number(
      parts.find((part) => part.type === "year")!.value
    ),
  };
}

function InsightCard({
  title,
  value,
  description,
  icon,
}: {
  title: string;
  value: string;
  description: string;
  icon: ReactNode;
}) {
  return (
    <Card className="interactive-surface min-w-0 p-3.5 sm:p-4">
      <div className="flex items-center justify-between gap-2">
        <h3 className="min-w-0 text-sm font-medium text-[var(--color-text-secondary)]">
          {title}
        </h3>

        <span
          aria-hidden="true"
          className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-[var(--color-primary)]/10 text-[var(--color-primary)]"
        >
          {icon}
        </span>
      </div>

      <p className="mt-1.5 break-words text-2xl font-bold leading-tight tracking-tight text-[var(--color-text)]">
        {value}
      </p>

      <p className="mt-1 text-xs leading-4 text-[var(--color-text-secondary)]">
        {description}
      </p>
    </Card>
  );
}

function InsightSection({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  return (
    <section className="space-y-2">
      <h2 className="text-sm font-semibold text-[var(--color-text)]">
        {title}
      </h2>

      {children}
    </section>
  );
}

function LoadingCards({
  count,
  className,
}: {
  count: number;
  className: string;
}) {
  return (
    <div aria-hidden="true" className={className}>
      {Array.from({ length: count }, (_, index) => (
        <Card
          key={index}
          className="min-w-0 animate-pulse p-3.5 motion-reduce:animate-none sm:p-4"
        >
          <div className="flex items-center justify-between gap-2">
            <div className="h-3 w-20 max-w-full rounded bg-[var(--color-surface-hover)]" />

            <div className="h-8 w-8 shrink-0 rounded-lg bg-[var(--color-primary)]/10" />
          </div>

          <div className="mt-2 h-6 w-24 max-w-full rounded bg-[var(--color-surface-hover)]" />

          <div className="mt-2 h-3 w-28 max-w-full rounded bg-[var(--color-surface-hover)]" />
        </Card>
      ))}
    </div>
  );
}

function LoadingInsights({
  view,
}: {
  view: InsightsView;
}) {
  return (
    <div role="status" className="space-y-4">
      <span className="sr-only">
        Loading insights...
      </span>

      {view === "charts" ? (
        <div
          aria-hidden="true"
          className="grid gap-4 lg:grid-cols-3"
        >
          {Array.from({ length: 3 }, (_, index) => (
            <Card
              key={index}
              className="min-w-0 animate-pulse p-4 motion-reduce:animate-none sm:p-5"
            >
              <div className="h-4 w-32 rounded bg-[var(--color-surface-hover)]" />

              <div className="mt-2 h-3 w-48 max-w-full rounded bg-[var(--color-surface-hover)]" />

              <div className="mx-auto my-5 h-52 w-52 max-w-full rounded-full bg-[var(--color-surface-hover)]" />

              <div className="h-3 w-full rounded bg-[var(--color-surface-hover)]" />

              <div className="mt-3 h-3 w-full rounded bg-[var(--color-surface-hover)]" />
            </Card>
          ))}
        </div>
      ) : (
        <>
          <InsightSection title="Financial overview">
            <LoadingCards
              count={4}
              className={summaryGrid}
            />
          </InsightSection>

          <InsightSection title="Bills & customers">
            <LoadingCards
              count={4}
              className={summaryGrid}
            />
          </InsightSection>

          <InsightSection title="Meal overview">
            <LoadingCards
              count={5}
              className={mealGrid}
            />
          </InsightSection>
        </>
      )}
    </div>
  );
}

export default function InsightsPage() {
  const [period, setPeriod] = useState(getCurrentPeriod);
  const [view, setView] = useState<InsightsView>("cards");

  const { month, year } = period;

  const {
    insights,
    loading,
    error,
    fetchInsights,
  } = useInsights(month, year);

  const periodLabel = new Intl.DateTimeFormat("en-IN", {
    month: "long",
    year: "numeric",
    timeZone: "UTC",
  }).format(new Date(Date.UTC(year, month - 1, 1)));

  const financial = insights?.financial;
  const meals = insights?.meals;

  return (
    <section className="space-y-4">
      <PageHeader
  title="Insights"
  description={`Business overview for ${periodLabel}.`}
  action={
    <div className="flex w-full flex-wrap items-center justify-end gap-2 sm:w-auto">
      <div className="min-w-0 flex-1 sm:flex-none">
        <InsightsFilters
          month={month}
          year={year}
          onMonthChange={(value) =>
            setPeriod((previous) => ({
              ...previous,
              month: value,
            }))
          }
          onYearChange={(value) =>
            setPeriod((previous) => ({
              ...previous,
              year: value,
            }))
          }
        />
      </div>

      <Button
        type="button"
        variant="outline"
        className="shrink-0"
        title={
          view === "cards"
            ? "Switch to chart view"
            : "Switch to card view"
        }
        onClick={() =>
          setView((previous) =>
            previous === "cards" ? "charts" : "cards"
          )
        }
      >
        {view === "cards" ? (
          <ChartPie size={16} aria-hidden="true" />
        ) : (
          <LayoutGrid size={16} aria-hidden="true" />
        )}

        {view === "cards" ? "Chart View" : "Card View"}
      </Button>
    </div>
  }
/>

      {loading ? (
        <LoadingInsights view={view} />
      ) : error || !insights || !financial || !meals ? (
        <Card>
          <Card.Body className="py-10 text-center">
            <div role="alert">
              <h2 className="text-lg font-semibold text-[var(--color-text)]">
                Unable to load insights
              </h2>

              <p className="mx-auto mt-2 max-w-md text-sm text-[var(--color-text-secondary)]">
                {error ||
                  "Insights are unavailable for this period."}
              </p>
            </div>

            <Button
              type="button"
              variant="secondary"
              className="mt-5"
              onClick={() => void fetchInsights()}
            >
              Retry
            </Button>
          </Card.Body>
        </Card>
      ) : view === "charts" ? (
        <InsightsCharts insights={insights} />
      ) : (
        <div className="space-y-4">
          <InsightSection title="Financial overview">
            <div className={summaryGrid}>
              <InsightCard
                title="Total billed"
                value={currencyFormat.format(
                  financial.totalRevenue
                )}
                description="Generated bill value"
                icon={<IndianRupee size={17} />}
              />

              <InsightCard
                title="Collected"
                value={currencyFormat.format(
                  financial.collectedRevenue
                )}
                description="Payments received"
                icon={<Wallet size={17} />}
              />

              <InsightCard
                title="Outstanding"
                value={currencyFormat.format(
                  financial.pendingRevenue
                )}
                description="Awaiting payment"
                icon={<Clock3 size={17} />}
              />

              <InsightCard
                title="Collection rate"
                value={`${financial.collectionRate.toLocaleString(
                  "en-IN",
                  { maximumFractionDigits: 1 }
                )}%`}
                description="Billed value collected"
                icon={<Percent size={17} />}
              />
            </div>
          </InsightSection>

          <InsightSection title="Bills & customers">
            <div className={summaryGrid}>
              <InsightCard
                title="Bills generated"
                value={countFormat.format(
                  financial.billsGenerated
                )}
                description="In this billing period"
                icon={<Receipt size={17} />}
              />

              <InsightCard
                title="Paid bills"
                value={countFormat.format(
                  financial.paidBills
                )}
                description="Payment completed"
                icon={<CheckCircle2 size={17} />}
              />

              <InsightCard
                title="Unpaid bills"
                value={countFormat.format(
                  financial.pendingBills
                )}
                description="Payment pending"
                icon={<Clock3 size={17} />}
              />

              <InsightCard
                title="Billed customers"
                value={countFormat.format(
                  insights.customers.activeCustomers
                )}
                description="Distinct customers billed"
                icon={<Users size={17} />}
              />
            </div>
          </InsightSection>

          <InsightSection title="Meal overview">
            <div className={mealGrid}>
              <InsightCard
                title="Total meals"
                value={countFormat.format(
                  meals.totalMeals
                )}
                description="Full and half meals"
                icon={<UtensilsCrossed size={17} />}
              />

              <InsightCard
                title="Full meals"
                value={countFormat.format(
                  meals.fullMeals
                )}
                description="Full portions"
                icon={<UtensilsCrossed size={17} />}
              />

              <InsightCard
                title="Half meals"
                value={countFormat.format(
                  meals.halfMeals
                )}
                description="Half portions"
                icon={<Soup size={17} />}
              />

              <InsightCard
                title="Total rotis"
                value={countFormat.format(
                  meals.totalRotis
                )}
                description="Including extras"
                icon={<Wheat size={17} />}
              />

              <InsightCard
                title="Extra rotis"
                value={countFormat.format(
                  meals.extraRotis
                )}
                description="Additional rotis"
                icon={<CirclePlus size={17} />}
              />
            </div>
          </InsightSection>

          <p
            role={
              financial.billsGenerated === 0 &&
              meals.totalMeals === 0
                ? "status"
                : undefined
            }
            className="text-xs leading-5 text-[var(--color-text-secondary)]"
          >
            {financial.billsGenerated === 0 &&
            meals.totalMeals === 0
              ? "No billed activity for this period. Figures appear after bill generation."
              : "Figures reflect the selected billing period. Meal totals include billed collections."}
          </p>
        </div>
      )}
    </section>
  );
}
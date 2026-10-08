import { useMemo, useState } from "react";

import { Plus, RefreshCw, Search } from "lucide-react";

import {
  Button,
  Input,
  Modal,
  PageHeader,
} from "@/components/common/ui";

import {
  BillingSummary,
  BillingTable,
  GenerateBillCard,
  BillDetailsDialog,
} from "../components";

import {
  useBillingOverview,
  useGenerateBills,
  useBillDetails,
} from "../hooks";

import type { GenerateBillRequest } from "../types";

type GenerationFilters = Omit<
  GenerateBillRequest,
  "billingMonth" | "billingYear"
>;

type StatusFilter = "ALL" | "UNPAID" | "PAID";

const MONTHS = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
];

const selectClassName =
  "h-10 min-w-0 rounded-lg border border-[var(--color-border)] " +
  "bg-[var(--color-surface)] px-3 text-sm text-[var(--color-text)] " +
  "focus-visible:outline-none focus-visible:ring-2 " +
  "focus-visible:ring-[var(--color-primary)]";

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

export default function BillingPage() {
  const [allPeriods, setAllPeriods] = useState(false);

  const [period, setPeriod] = useState(getCurrentPeriod);
  const billingMonth = period.month;
  const billingYear = period.year;

  const [search, setSearch] = useState("");

  const [statusFilter, setStatusFilter] =
    useState<StatusFilter>("ALL");

  const [generationOpen, setGenerationOpen] = useState(false);

  const [generationMonth, setGenerationMonth] =
    useState(() => getCurrentPeriod().month);

  const [generationYear, setGenerationYear] =
    useState(() => getCurrentPeriod().year);

  const [selectedBillId, setSelectedBillId] =
    useState<number | null>(null);

  const {
    overview,
    loading: overviewLoading,
    error: overviewError,
    refreshBillingOverview,
  } = useBillingOverview(
    allPeriods ? null : billingMonth,
    allPeriods ? null : billingYear
  );

  const {
    loading: generating,
    generateBills,
  } = useGenerateBills(refreshBillingOverview);

  const {
    bill,
    loading: billLoading,
    error: billError,
    refreshBillDetails,
  } = useBillDetails(selectedBillId);

  const filteredBills = useMemo(() => {
    const query = search.trim().toLowerCase();

    return (overview?.bills ?? []).filter((item) => {
      if (
        statusFilter !== "ALL" &&
        item.billStatus !== statusFilter
      ) {
        return false;
      }

      if (!query) {
        return true;
      }

      const searchableText = [
        item.billId,
        `#${item.billId}`,
        item.customerId,
        item.customerName,
        MONTHS[item.billingMonth - 1],
        item.billingYear,
        `${item.billingMonth}/${item.billingYear}`,
        `${String(item.billingMonth).padStart(2, "0")}/${item.billingYear}`,
        item.billStatus,
        item.totalAmount,
      ]
        .join(" ")
        .toLowerCase();

      return searchableText.includes(query);
    });
  }, [overview, search, statusFilter]);

  const currentYear = getCurrentPeriod().year;

  const yearOptions = useMemo(() => {
    const years = new Set<number>([
      ...Array.from(
        { length: 5 },
        (_, index) => currentYear - index
      ),
      billingYear,
    ]);

    for (const item of overview?.bills ?? []) {
      years.add(item.billingYear);
    }

    return [...years].sort((first, second) => second - first);
  }, [overview, billingYear, currentYear]);

  async function handleGenerate(
    filters: GenerationFilters = {}
  ): Promise<boolean> {
    const success = await generateBills(
      generationMonth,
      generationYear,
      filters
    );

    if (success) {
      setGenerationOpen(false);
    }

    return success;
  }

  async function handleRefresh() {
    try {
      await refreshBillingOverview();
    } catch {
      // The hook exposes the error for display.
    }
  }

  async function handleRetryDetails() {
    try {
      await refreshBillDetails();
    } catch {
      // The hook exposes the error for display.
    }
  }

  function openGeneration() {
    const currentPeriod = getCurrentPeriod();

    setGenerationMonth(
      allPeriods ? currentPeriod.month : billingMonth
    );

    setGenerationYear(
      allPeriods ? currentPeriod.year : billingYear
    );

    setGenerationOpen(true);
  }

  const periodLabel = allPeriods
    ? "All billing periods"
    : `${MONTHS[billingMonth - 1]} ${billingYear}`;

  return (
    <>
      <section className="space-y-4">
        <PageHeader
          title="Bills"
          description="Manage bills, outstanding balances, and payments."
          action={
            <div className="flex max-w-full flex-wrap items-center gap-2">
              <select
                aria-label="Billing period scope"
                value={allPeriods ? "ALL" : "MONTH"}
                onChange={(event) =>
                  setAllPeriods(event.target.value === "ALL")
                }
                className={selectClassName}
              >
                <option value="MONTH">Monthly</option>
                <option value="ALL">All periods</option>
              </select>

              {!allPeriods && (
                <>
                  <select
                    aria-label="Billing month"
                    value={billingMonth}
                    onChange={(event) =>
                      setPeriod((previous) => ({
                        ...previous,
                        month: Number(event.target.value),
                      }))
                    }
                    className={`${selectClassName} w-32`}
                  >
                    {MONTHS.map((month, index) => (
                      <option key={month} value={index + 1}>
                        {month}
                      </option>
                    ))}
                  </select>

                  <select
                    aria-label="Billing year"
                    value={billingYear}
                    onChange={(event) =>
                      setPeriod((previous) => ({
                        ...previous,
                        year: Number(event.target.value),
                      }))
                    }
                    className={`${selectClassName} w-24`}
                  >
                    {yearOptions.map((year) => (
                      <option key={year} value={year}>
                        {year}
                      </option>
                    ))}
                  </select>
                </>
              )}

              <Button
                type="button"
                variant="outline"
                disabled={overviewLoading || generating}
                title="Fetch the latest bills and payment records"
                onClick={() => void handleRefresh()}
              >
              <RefreshCw
                size={16}
                aria-hidden="true"
                className={
                overviewLoading
                  ? "animate-spin motion-reduce:animate-none"
                  : ""
                }
              />

              </Button>

              <Button
                type="button"
                disabled={generating}
                onClick={openGeneration}
              >
                <Plus size={16} aria-hidden="true" />
                Generate Bill
              </Button>
            </div>
          }
        />

        {overviewError && (
          <div
            role="alert"
            className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4"
          >
            <p className="text-sm text-[var(--color-danger)]">
              {overviewError}
            </p>

            <Button
              type="button"
              variant="secondary"
              size="sm"
              disabled={overviewLoading || generating}
              onClick={() => void handleRefresh()}
            >
              Retry
            </Button>
          </div>
        )}

        {overviewLoading ? (
          <div
            role="status"
            className="py-10 text-center text-sm text-[var(--color-text-secondary)]"
          >
            Loading bills...
          </div>
        ) : overview ? (
          <>
            <section className="space-y-2">
              <h2 className="text-sm font-semibold text-[var(--color-text)]">
                {periodLabel}
              </h2>

              <BillingSummary summary={overview.summary} />

              <p className="text-xs leading-5 text-[var(--color-text-secondary)]">
                Collections reflect payments against these bills,
                regardless of payment date.
              </p>
            </section>

            <section className="space-y-3">
              <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <div className="w-full sm:max-w-sm">
                  <Input
                    fullWidth
                    value={search}
                    onChange={(event) =>
                      setSearch(event.target.value)
                    }
                    aria-label="Search bills"
                    placeholder="Search customer, bill or period..."
                    leftIcon={<Search size={17} />}
                  />
                </div>

                <div className="flex items-center justify-between gap-3 sm:justify-end">
                  <p className="text-xs text-[var(--color-text-secondary)]">
                    {filteredBills.length} of{" "}
                    {overview.bills.length} bills
                  </p>

                  <select
                    aria-label="Bill status"
                    value={statusFilter}
                    onChange={(event) =>
                      setStatusFilter(
                        event.target.value as StatusFilter
                      )
                    }
                    className={selectClassName}
                  >
                    <option value="ALL">All statuses</option>
                    <option value="UNPAID">Unpaid</option>
                    <option value="PAID">Paid</option>
                  </select>
                </div>
              </div>

              {filteredBills.length > 0 ? (
                <BillingTable
                  bills={filteredBills}
                  onViewBill={setSelectedBillId}
                />
              ) : (
                <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-8 text-center text-sm text-[var(--color-text-secondary)]">
                  {search.trim() || statusFilter !== "ALL"
                    ? "No bills match your filters."
                    : "No bills generated for this period."}
                </div>
              )}

              <p className="text-xs leading-5 text-[var(--color-text-secondary)]">
                Search and status filters apply to the table.
                Monthly billing runs automatically when enabled;
                use Generate Bill for early billing or additional
                unbilled meals.
              </p>
            </section>
          </>
        ) : !overviewError ? (
          <p className="py-10 text-center text-sm text-[var(--color-text-secondary)]">
            Billing overview is unavailable. Use Refresh to try again.
          </p>
        ) : null}
      </section>

      <Modal
        open={generationOpen}
        title="Generate Bill"
        size="lg"
        onClose={() => {
          if (!generating) {
            setGenerationOpen(false);
          }
        }}
      >
        {generationOpen && (
          <GenerateBillCard
            billingMonth={generationMonth}
            billingYear={generationYear}
            loading={generating}
            onMonthChange={setGenerationMonth}
            onYearChange={setGenerationYear}
            onGenerate={handleGenerate}
          />
        )}
      </Modal>

      <BillDetailsDialog
        open={selectedBillId !== null}
        bill={bill}
        loading={billLoading}
        error={billError}
        onRetry={() => void handleRetryDetails()}
        onClose={() => setSelectedBillId(null)}
      />
    </>
  );
}
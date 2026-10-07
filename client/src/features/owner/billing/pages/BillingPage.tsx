import { useMemo, useState } from "react";

import { RefreshCw, Search } from "lucide-react";

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
  "h-10 rounded-lg border border-[var(--color-border)] " +
  "bg-[var(--color-surface)] px-3 text-sm text-[var(--color-text)]";

export default function BillingPage() {
  const [allPeriods, setAllPeriods] = useState(false);

  const [billingMonth, setBillingMonth] =
    useState(() => new Date().getMonth() + 1);

  const [billingYear, setBillingYear] =
    useState(() => new Date().getFullYear());

  const [search, setSearch] = useState("");

  const [statusFilter, setStatusFilter] =
    useState<StatusFilter>("ALL");

  const [generationOpen, setGenerationOpen] =
    useState(false);

  const [generationMonth, setGenerationMonth] =
    useState(() => new Date().getMonth() + 1);

  const [generationYear, setGenerationYear] =
    useState(() => new Date().getFullYear());

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

  const yearOptions = useMemo(() => {
    const years = new Set<number>([
      new Date().getFullYear(),
      billingYear,
    ]);

    for (const item of overview?.bills ?? []) {
      years.add(item.billingYear);
    }

    return [...years].sort((first, second) => second - first);
  }, [overview, billingYear]);

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
    const now = new Date();

    setGenerationMonth(
      allPeriods ? now.getMonth() + 1 : billingMonth
    );

    setGenerationYear(
      allPeriods ? now.getFullYear() : billingYear
    );

    setGenerationOpen(true);
  }

  const periodLabel = allPeriods
    ? "All billing periods"
    : `${MONTHS[billingMonth - 1]} ${billingYear}`;

  return (
    <>
      <div className="space-y-6">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <PageHeader
            title="Bills"
            description="Manage customer bills, outstanding balances and payment records."
          />

          <div className="flex flex-wrap gap-2">
            <Button
              variant="secondary"
              disabled={overviewLoading || generating}
              onClick={() => void handleRefresh()}
            >
              <RefreshCw
                size={16}
                className={
                  overviewLoading ? "animate-spin" : ""
                }
              />

              {overviewLoading ? "Refreshing..." : "Refresh"}
            </Button>

            <Button
              disabled={generating}
              onClick={openGeneration}
            >
              Generate Bill
            </Button>
          </div>
        </div>

        <section className="space-y-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
          <div className="flex flex-wrap items-center gap-3">
            <select
              aria-label="Billing period scope"
              value={allPeriods ? "ALL" : "MONTH"}
              onChange={(event) =>
                setAllPeriods(event.target.value === "ALL")
              }
              className={selectClassName}
            >
              <option value="ALL">All periods</option>
              <option value="MONTH">Selected month</option>
            </select>

            {!allPeriods && (
              <>
                <select
                  aria-label="Billing month"
                  value={billingMonth}
                  onChange={(event) =>
                    setBillingMonth(Number(event.target.value))
                  }
                  className={selectClassName}
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
                    setBillingYear(Number(event.target.value))
                  }
                  className={selectClassName}
                >
                  {yearOptions.map((year) => (
                    <option key={year} value={year}>
                      {year}
                    </option>
                  ))}
                </select>
              </>
            )}
          </div>

          <p className="text-xs leading-5 text-[var(--color-text-secondary)]">
            Monthly bills are generated automatically when
            billing automation is enabled. Use Generate Bill
            for early billing or additional unbilled meals,
            including an individual customer.
          </p>
        </section>

        {overviewError && (
          <div
            role="alert"
            className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-red-200 p-4"
          >
            <p className="text-sm text-red-500">
              {overviewError}
            </p>

            <Button
              variant="secondary"
              size="sm"
              disabled={overviewLoading || generating}
              onClick={() => void handleRefresh()}
            >
              Retry
            </Button>
          </div>
        )}

        {overviewLoading && !overview && (
          <p className="text-sm text-[var(--color-text-secondary)]">
            Loading bills...
          </p>
        )}

        {overview && (
          <>
            <div className="space-y-3">
              <p className="text-sm font-medium">
                {periodLabel}
              </p>

              <BillingSummary summary={overview.summary} />

              <p className="text-xs text-[var(--color-text-secondary)]">
                Summary totals cover {periodLabel.toLowerCase()}.
                Collections are recorded payments against those
                bills, regardless of payment date. Search and
                status filters apply to the table below.
              </p>
            </div>

            <section className="space-y-4">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
                <div className="flex-1">
                  <Input
                    fullWidth
                    value={search}
                    onChange={(event) =>
                      setSearch(event.target.value)
                    }
                    placeholder="Search customer, bill or period..."
                    leftIcon={<Search size={18} />}
                  />
                </div>

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

              <p className="text-sm text-[var(--color-text-secondary)]">
                Showing {filteredBills.length} of{" "}
                {overview.bills.length} bills
              </p>

              {filteredBills.length > 0 ? (
                <BillingTable
                  bills={filteredBills}
                  onViewBill={setSelectedBillId}
                />
              ) : (
                <div className="rounded-xl border border-[var(--color-border)] p-8 text-center text-sm text-[var(--color-text-secondary)]">
                  {search.trim() || statusFilter !== "ALL"
                    ? "No bills match your filters."
                    : "No bills generated for this period."}
                </div>
              )}
            </section>
          </>
        )}
      </div>

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
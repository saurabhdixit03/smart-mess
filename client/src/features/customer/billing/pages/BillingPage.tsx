import { useMemo, useState } from "react";

import { Search } from "lucide-react";

import {
  Button,
  Card,
  Input,
  PageHeader,
} from "@/components/common/ui";

import BillsList from "../components/BillsList";

import { useBills } from "../hooks";

import { getBillReference } from "@/features/billing/utils/getBillReference";

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

export default function BillingPage() {
  const {
    bills,
    loading,
    error,
    fetchBills,
  } = useBills();

  const [search, setSearch] = useState("");

  const [statusFilter, setStatusFilter] =
    useState<StatusFilter>("ALL");

  const filteredBills = useMemo(() => {
    const query = search.trim().toLowerCase();

    return bills.filter((bill) => {
      if (
        statusFilter !== "ALL" &&
        bill.billStatus !== statusFilter
      ) {
        return false;
      }

      if (!query) {
        return true;
      }

      const searchableText = [
        bill.billId,
        `#${bill.billId}`,
        MONTHS[bill.billingMonth - 1],
        bill.billingYear,
        `${bill.billingMonth}/${bill.billingYear}`,
        `${String(bill.billingMonth).padStart(2, "0")}/${bill.billingYear}`,
        bill.billStatus,
        bill.totalAmount,
        getBillReference(bill),
      ]
        .join(" ")
        .toLowerCase();

      return searchableText.includes(query);
    });
  }, [bills, search, statusFilter]);

  return (
    <section className="min-w-0 space-y-4">
      <PageHeader
        title="My Bills"
        description="View your bills and payment history."
      />

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="w-full sm:max-w-sm">
          <Input
            fullWidth
            aria-label="Search bills"
            placeholder="Search bill, month or amount..."
            leftIcon={<Search size={18} />}
            value={search}
            onChange={(event) =>
              setSearch(event.target.value)
            }
          />
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {!loading && !error && (
            <p className="text-sm text-[var(--color-text-secondary)]">
              Showing{" "}
              <span className="font-medium text-[var(--color-text)]">
                {filteredBills.length}
              </span>{" "}
              of {bills.length} bills
            </p>
          )}

          <select
            aria-label="Bill status"
            value={statusFilter}
            onChange={(event) =>
              setStatusFilter(
                event.target.value as StatusFilter
              )
            }
            className="h-10 rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] px-3 text-sm text-[var(--color-text)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]"
          >
            <option value="ALL">All statuses</option>
            <option value="UNPAID">Unpaid</option>
            <option value="PAID">Paid</option>
          </select>
        </div>
      </div>

      {loading ? (
        <Card>
          <Card.Body className="py-10 text-center">
            <p
              role="status"
              className="text-sm text-[var(--color-text-secondary)]"
            >
              Loading bills...
            </p>
          </Card.Body>
        </Card>
      ) : error ? (
        <Card>
          <Card.Body className="space-y-3 py-8 text-center">
            <p
              role="alert"
              className="text-sm text-[var(--color-danger)]"
            >
              {error}
            </p>

            <Button
              type="button"
              variant="secondary"
              size="sm"
              onClick={() => void fetchBills()}
            >
              Retry
            </Button>
          </Card.Body>
        </Card>
      ) : (
        <>
          {filteredBills.length === 0 && (
            <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-8 text-center text-sm text-[var(--color-text-secondary)]">
              {search.trim() || statusFilter !== "ALL"
                ? "No bills match your filters."
                : "No bills have been generated yet."}
            </div>
          )}

          <BillsList
            bills={filteredBills}
            onPaymentConfirmed={fetchBills}
          />
        </>
      )}
    </section>
  );
}
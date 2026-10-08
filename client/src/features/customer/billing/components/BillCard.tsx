import { Receipt } from "lucide-react";

import {
  Button,
  Card,
  StatusBadge,
} from "@/components/common/ui";

import type { Bill } from "../types";

import { getBillReference } from "@/features/billing/utils/getBillReference";

interface BillCardProps {
  bill: Bill;
  onView: (billId: number) => void;
  onPay: (billId: number) => void;
}

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

const currencyFormat = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
});

function formatDate(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return date.toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

export default function BillCard({
  bill,
  onView,
  onPay,
}: BillCardProps) {
  const unpaid = bill.billStatus === "UNPAID";

  return (
    <Card className="interactive-surface flex h-full flex-col">
      <Card.Body className="flex flex-1 flex-col p-4">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-[var(--color-border)] pb-3">
          <div className="flex min-w-0 items-center gap-3">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[var(--color-primary)]/10 text-[var(--color-primary)]">
              <Receipt
                size={20}
                aria-hidden="true"
              />
            </div>

            <div>
              <h2 className="text-base font-semibold tracking-tight text-[var(--color-text)]">
                {MONTHS[bill.billingMonth - 1]}{" "}
                {bill.billingYear}
              </h2>

              <p className="text-xs text-[var(--color-text-secondary)]">
                {getBillReference(bill)}
              </p>
            </div>
          </div>

          <StatusBadge
            label={unpaid ? "Unpaid" : "Paid"}
            variant={unpaid ? "warning" : "success"}
          />
        </div>

        <div className="mt-3 rounded-xl border border-[var(--color-border)] bg-[#FAFAF8] p-3">
          <p className="text-xs text-[var(--color-text-secondary)]">
            Total amount
          </p>

          <p className="mt-1 break-words text-2xl font-bold tracking-tight tabular-nums text-[var(--color-text)]">
            {currencyFormat.format(bill.totalAmount)}
          </p>

          <div className="mt-2 flex flex-wrap items-center justify-between gap-2 text-xs text-[var(--color-text-secondary)]">
            <span>
              {bill.mealRecordCount}{" "}
              {bill.mealRecordCount === 1
                ? "meal"
                : "meals"}
            </span>

            <span>
              Issued {formatDate(bill.generatedAt)}
            </span>
          </div>
        </div>
      </Card.Body>

      <Card.Footer className="px-4 py-3">
        <div
          className={
            unpaid
              ? "grid grid-cols-2 gap-2"
              : "grid grid-cols-1"
          }
        >
          <Button
            type="button"
            fullWidth
            size="sm"
            variant="secondary"
            onClick={() => onView(bill.billId)}
          >
            View Bill
          </Button>

          {unpaid && (
            <Button
              type="button"
              fullWidth
              size="sm"
              onClick={() => onPay(bill.billId)}
            >
              Pay Now
            </Button>
          )}
        </div>
      </Card.Footer>
    </Card>
  );
}
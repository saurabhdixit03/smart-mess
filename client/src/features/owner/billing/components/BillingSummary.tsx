import type { ReactNode } from "react";

import {
  CheckCircle2,
  Clock3,
  FileText,
  IndianRupee,
  Wallet,
} from "lucide-react";

import { Card } from "@/components/common/ui";

import type { BillingSummaryResponse } from "../types";

type BillingSummaryProps = {
  summary: BillingSummaryResponse;
};

const currencyFormat = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

const countFormat = new Intl.NumberFormat("en-IN");

function SummaryCard({
  title,
  value,
  icon,
}: {
  title: string;
  value: string;
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

      <p className="mt-2 break-words text-2xl font-bold leading-tight tracking-tight text-[var(--color-text)]">
        {value}
      </p>
    </Card>
  );
}

export default function BillingSummary({
  summary,
}: BillingSummaryProps) {
  return (
    <section
      aria-label="Billing summary"
      className="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-6"
    >
      <SummaryCard
        title="Total bills"
        value={countFormat.format(summary.totalBills)}
        icon={<FileText size={17} />}
      />

      <SummaryCard
        title="Paid bills"
        value={countFormat.format(summary.paidBills)}
        icon={<CheckCircle2 size={17} />}
      />

      <SummaryCard
        title="Unpaid bills"
        value={countFormat.format(summary.unpaidBills)}
        icon={<Clock3 size={17} />}
      />

      <SummaryCard
        title="Total billed"
        value={currencyFormat.format(Number(summary.totalRevenue))}
        icon={<IndianRupee size={17} />}
      />

      <SummaryCard
        title="Collected"
        value={currencyFormat.format(
          Number(summary.collectedRevenue)
        )}
        icon={<Wallet size={17} />}
      />

      <SummaryCard
        title="Outstanding"
        value={currencyFormat.format(
          Number(summary.pendingRevenue)
        )}
        icon={<Clock3 size={17} />}
      />
    </section>
  );
}
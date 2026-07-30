import {
  BadgeDollarSign,
  CheckCircle2,
  Clock3,
  FileText,
  Wallet,
  IndianRupee,
} from "lucide-react";

import StatsCard from "@/components/common/ui/StatsCard";

import type { BillingSummaryResponse } from "../types";

type BillingSummaryProps = {
  summary: BillingSummaryResponse;
};

export default function BillingSummary({
  summary,
}: BillingSummaryProps) {
  return (
    <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      <StatsCard
        title="Total Bills"
        value={summary.totalBills}
        icon={<FileText size={24} />}
      />

      <StatsCard
        title="Paid Bills"
        value={summary.paidBills}
        icon={<CheckCircle2 size={24} />}
      />

      <StatsCard
        title="Unpaid Bills"
        value={summary.unpaidBills}
        icon={<Clock3 size={24} />}
      />

      <StatsCard
        title="Total Revenue"
        value={`₹${summary.totalRevenue}`}
        icon={<BadgeDollarSign size={24} />}
      />

      <StatsCard
        title="Collected Revenue"
        value={`₹${summary.collectedRevenue}`}
        icon={<Wallet size={24} />}
      />

      <StatsCard
        title="Pending Revenue"
        value={`₹${summary.pendingRevenue}`}
        icon={<IndianRupee size={24} />}
      />
    </section>
  );
}
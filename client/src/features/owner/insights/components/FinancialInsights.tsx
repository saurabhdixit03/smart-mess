import StatsCard from "@/components/common/ui/StatsCard";

import {
  Receipt,
  CheckCircle2,
  Clock3,
  IndianRupee,
  Wallet,
  Percent,
} from "lucide-react";

import type {
  FinancialInsightsResponse,
} from "../types";

type FinancialInsightsProps = {
  financial: FinancialInsightsResponse;
};

export default function FinancialInsights({
  financial,
}: FinancialInsightsProps) {

  return (

    <div className="space-y-4">

      <div>

        <h2 className="text-lg font-semibold">
          Financial Insights
        </h2>

        <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
          Monthly billing and revenue summary.
        </p>

      </div>

      <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">

        <StatsCard
          title="Bills Generated"
          value={financial.billsGenerated}
          description="Generated this month"
          icon={<Receipt size={26} />}
        />

        <StatsCard
          title="Paid Bills"
          value={financial.paidBills}
          description="Successfully collected"
          icon={<CheckCircle2 size={26} />}
        />

        <StatsCard
          title="Pending Bills"
          value={financial.pendingBills}
          description="Awaiting payment"
          icon={<Clock3 size={26} />}
        />

        <StatsCard
          title="Total Revenue"
          value={`₹${Number(
            financial.totalRevenue
          ).toLocaleString("en-IN")}`}
          description="Generated revenue"
          icon={<IndianRupee size={26} />}
        />

        <StatsCard
          title="Collected Revenue"
          value={`₹${Number(
            financial.collectedRevenue
          ).toLocaleString("en-IN")}`}
          description="Amount received"
          icon={<Wallet size={26} />}
        />

        <StatsCard
          title="Collection Rate"
          value={`${financial.collectionRate}%`}
          description="Recovery percentage"
          icon={<Percent size={26} />}
        />

      </div>

    </div>

  );

}
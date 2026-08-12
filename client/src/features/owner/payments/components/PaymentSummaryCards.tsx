import {
  CircleDollarSign,
  Clock3,
  CreditCard,
  Wallet,
} from "lucide-react";

import StatsCard from "@/components/common/ui/StatsCard";

type PaymentSummaryCardsProps = {
  unpaidBillCount: number;
  pendingRequestCount: number;
  paidBillCount: number;
  totalCollectedAmount: number;
};

export default function PaymentSummaryCards({
  unpaidBillCount,
  pendingRequestCount,
  paidBillCount,
  totalCollectedAmount,
}: PaymentSummaryCardsProps) {
  return (
    <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-4">

      <StatsCard
        title="Unpaid Bills"
        value={unpaidBillCount}
        description="Awaiting payment"
        icon={<Wallet size={26} />}
      />

      <StatsCard
        title="UPI Requests"
        value={pendingRequestCount}
        description="Pending approval"
        icon={<Clock3 size={26} />}
      />

      <StatsCard
        title="Payments Collected"
        value={paidBillCount}
        description="Successfully collected"
        icon={<CreditCard size={26} />}
      />

      <StatsCard
        title="Total Collected"
        value={`₹${totalCollectedAmount.toLocaleString("en-IN")}`}
        description="Revenue received"
        icon={<CircleDollarSign size={26} />}
      />

    </div>
  );
}
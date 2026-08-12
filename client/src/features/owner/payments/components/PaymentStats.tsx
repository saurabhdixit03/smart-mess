import StatsCard from "@/components/common/ui/StatsCard";

import {
  Wallet,
  Clock3,
  CheckCircle2,
  IndianRupee,
} from "lucide-react";

import type { PaymentOverviewResponse } from "../types";

interface PaymentStatsProps {
  overview: PaymentOverviewResponse;
}

const PaymentStats = ({
  overview,
}: PaymentStatsProps) => {

  return (

    <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-4">

      <StatsCard
        title="Unpaid Bills"
        value={overview.unpaidBillCount}
        description="Awaiting payment"
        icon={<Wallet size={26} />}
      />

      <StatsCard
        title="Pending Requests"
        value={overview.pendingRequestCount}
        description="UPI verification requests"
        icon={<Clock3 size={26} />}
      />

      <StatsCard
        title="Paid Bills"
        value={overview.paidBillCount}
        description="Payments collected"
        icon={<CheckCircle2 size={26} />}
      />

      <StatsCard
        title="Collected Revenue"
        value={`₹${overview.totalCollectedAmount}`}
        description="Total received"
        icon={<IndianRupee size={26} />}
      />

    </div>

  );

};

export default PaymentStats;
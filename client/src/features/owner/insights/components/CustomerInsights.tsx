import StatsCard from "@/components/common/ui/StatsCard";

import { Users } from "lucide-react";

import type {
  CustomerInsightsResponse,
} from "../types";

type CustomerInsightsProps = {
  customers: CustomerInsightsResponse;
};

export default function CustomerInsights({
  customers,
}: CustomerInsightsProps) {

  return (

    <div className="space-y-4">

      <div>

        <h2 className="text-lg font-semibold">
          Customer Insights
        </h2>

        <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
          Active customer summary for the selected month.
        </p>

      </div>

      <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-4">

        <StatsCard
          title="Active Customers"
          value={customers.activeCustomers}
          description="Currently active"
          icon={<Users size={26} />}
        />

      </div>

    </div>

  );

}
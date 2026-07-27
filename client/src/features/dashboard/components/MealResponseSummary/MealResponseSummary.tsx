import Card from "@/components/common/ui/Card/Card";

import SummaryStats from "./SummaryStats";
import LiveResponseList from "./LiveResponseList";
import ResponseProgress from "./ResponseProgress";

import type {
  DashboardSummary,
} from "../../types/dashboard.types";

type MealResponseSummaryProps = {
  dashboard: DashboardSummary;
};

export default function MealResponseSummary({
  dashboard,
}: MealResponseSummaryProps) {

  const respondedCustomers =
    dashboard.acceptedResponses +
    dashboard.declinedResponses;

  return (

    <Card className="interactive-surface">

      <Card.Header className="py-2.5">

        <h3 className="text-base font-semibold">

          Today's Overview

        </h3>

        

      </Card.Header>

      <Card.Body className="space-y-3 py-3">

        {/* Top Section */}

        <div className="grid gap-6 xl:grid-cols-12">

          {/* Left */}

          <div className="xl:col-span-7">

            <SummaryStats
              dashboard={dashboard}
            />

          </div>

          {/* Right */}

          <div className="xl:col-span-5 border-l border-[var(--color-border)] pl-6">

            <LiveResponseList
              responses={dashboard.recentActivities}
            />

          </div>

        </div>

        {/* Bottom */}

        <ResponseProgress
          totalCustomers={
            dashboard.activeCustomers
          }
          respondedCustomers={
            respondedCustomers
          }
        />

      </Card.Body>

    </Card>

  );

}
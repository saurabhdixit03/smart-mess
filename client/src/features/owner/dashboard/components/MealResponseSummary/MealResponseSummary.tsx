import Card from "@/components/common/ui/Card/Card";

import SummaryStats from "./SummaryStats";
import LiveResponseList from "./LiveResponseList";
import ResponseProgress from "./ResponseProgress";

import type {
  MenuResponse,
} from "@/features/owner/menu/types/menu.types";

import type {
  DashboardSummary,
} from "../../types/dashboard.types";

type MealResponseSummaryProps = {
  dashboard: DashboardSummary;
  menu: MenuResponse;
};

export default function MealResponseSummary({
  dashboard,
  menu,
}: MealResponseSummaryProps) {
  const respondedCustomers =
    dashboard.acceptedResponses +
    dashboard.declinedResponses;

  const sessionLabel =
    menu.mealSession === "LUNCH"
      ? "Lunch"
      : "Dinner";

  return (
    <Card className="interactive-surface">

      <Card.Header className="py-4">

        <div
          className="
            flex
            flex-col
            gap-5
            lg:flex-row
            lg:items-start
            lg:justify-between
          "
        >
          {/* Overview information */}
          <div>

            <h2 className="text-lg font-semibold text-[var(--color-text)]">
              {sessionLabel} Overview
            </h2>

            <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
              Live responses and kitchen preparation details.
            </p>

          </div>

          {/* Date, live state, and menu */}
          <div
            className="
              lg:max-w-[65%]
              lg:text-right
            "
          >
            <div
              className="
                flex
                flex-wrap
                items-center
                gap-x-4
                gap-y-2
                text-sm

                lg:justify-end
              "
            >
              <span className="font-medium text-[var(--color-text)]">
                {menu.menuDate}
              </span>

              <span
                className="
                  flex
                  items-center
                  gap-2
                  font-medium
                  text-[var(--color-success)]
                "
              >
                <span
                  className="
                    h-2
                    w-2
                    animate-pulse
                    rounded-full
                    bg-[var(--color-success)]
                  "
                />

                Live
              </span>
            </div>

            <div
              className="
                mt-4
                flex
                flex-wrap
                gap-2

                lg:justify-end
              "
            >
              <span
                className="
                  rounded-full
                  bg-green-100
                  px-3
                  py-1
                  text-xs
                  font-medium
                  text-green-700
                "
              >
                {menu.sabjiOne}
              </span>

              <span
                className="
                  rounded-full
                  bg-green-100
                  px-3
                  py-1
                  text-xs
                  font-medium
                  text-green-700
                "
              >
                {menu.sabjiTwo}
              </span>

              <span
                className="
                  rounded-full
                  bg-amber-100
                  px-3
                  py-1
                  text-xs
                  font-medium
                  text-amber-700
                "
              >
                {menu.dal}
              </span>

              <span
                className="
                  rounded-full
                  bg-blue-100
                  px-3
                  py-1
                  text-xs
                  font-medium
                  text-blue-700
                "
              >
                {menu.rice}
              </span>

              <span
                className="
                  rounded-full
                  bg-pink-100
                  px-3
                  py-1
                  text-xs
                  font-medium
                  text-pink-700
                "
              >
                {menu.sweet}
              </span>

            </div>

          </div>

        </div>

      </Card.Header>

      <Card.Body className="space-y-4 py-4">

        <div
          className="
            grid
            gap-5
            xl:grid-cols-12
          "
        >
          {/* Operational metrics */}
          <div className="xl:col-span-8">

            <SummaryStats
              dashboard={dashboard}
            />

          </div>

          {/* Latest responses */}
          <div
            className="
              border-t
              border-[var(--color-border)]
              pt-5

              xl:col-span-4
              xl:border-l
              xl:border-t-0
              xl:pl-5
              xl:pt-0
            "
          >
            <LiveResponseList
              responses={
                dashboard.recentActivities
              }
            />
          </div>

        </div>

        <ResponseProgress
          totalCustomers={
            dashboard.activeCustomers
          }
          acceptedCustomers={
            dashboard.acceptedResponses
          }
          declinedCustomers={
            dashboard.declinedResponses
          }
          pendingCustomers={
            dashboard.pendingResponses
          }
          respondedCustomers={
            respondedCustomers
          }
        />

      </Card.Body>

    </Card>
  );
}
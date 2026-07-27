
import RollingCounter from "@/components/common/business/RollingCounter";

import {
  CheckCheck,
  Clock3,
  XCircle,
  UtensilsCrossed,
  Soup,
  PlusCircle,
  DiameterIcon,
} from "lucide-react";

import type { DashboardSummary } from "../../types/dashboard.types";

type Props = {
  dashboard: DashboardSummary;
};

export default function SummaryStats({
  dashboard,
}: Props) {

  const responseStats = [

    {
      icon: CheckCheck,
      label: "Accepted",
      value: dashboard.acceptedResponses,
      color: "text-green-600",
    },

    {
      icon: Clock3,
      label: "Pending",
      value: dashboard.pendingResponses,
      color: "text-amber-500",
    },

    {
      icon: XCircle,
      label: "Declined",
      value: dashboard.declinedResponses,
      color: "text-red-500",
    },

  ];

  const preparationStats = [

    {
      icon: UtensilsCrossed,
      label: "Full",
      value: dashboard.expectedFullMeals,
      color: "text-orange-500",
    },

    {
      icon: Soup,
      label: "Half",
      value: dashboard.expectedHalfMeals,
      color: "text-blue-600",
    },

    {
      icon: DiameterIcon,
      label: "Base Rotis",
      value: dashboard.baseRotisRequired,
      color: "text-amber-500",
    },

    {
      icon: PlusCircle,
      label: "Extra Rotis",
      value: dashboard.expectedExtraRotis,
      color: "text-blue-600",
    },

    {
      icon: DiameterIcon,
      label: "Total Rotis",
      value: dashboard.totalRotisRequired,
      color: "text-orange-500",
    },

  ];

  return (

    <div className="space-y-2">

      {/* Response Metrics */}

      <div>

        <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-[var(--color-text-secondary)]">

          Responses

        </p>

        <div className="grid grid-cols-3">

          {responseStats.map((item) => (

            <div
              key={item.label}
              className="text-center"
            >

              <item.icon
                size={20}
                strokeWidth={2}
                className={`mx-auto ${item.color}`}
              />

              <div className="mt-1 text-xl font-bold text-[var(--color-primary)]">

                <RollingCounter value={item.value} />

              </div>

              <div className="mt-1 text-[11px] text-[var(--color-text-secondary)]">

                {item.label}

              </div>

            </div>

          ))}

        </div>

      </div>

      <div className="border-t border-[var(--color-border)]" />

      {/* Kitchen Preparation */}

      <div>

        <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-[var(--color-text-secondary)]">

          Kitchen Preparation

        </p>

        <div className="grid grid-cols-5">

          {preparationStats.map((item) => (

            <div
              key={item.label}
              className="text-center"
            >

              <item.icon
                size={18}
                strokeWidth={2}
                className={`mx-auto ${item.color}`}
              />

              <div className="mt-1 text-xl font-semibold text-[var(--color-primary)]">

                <RollingCounter value={item.value} />

              </div>

              <div className="mt-1 text-[11px] text-[var(--color-text-secondary)]">

                {item.label}

              </div>

            </div>

          ))}

        </div>

      </div>

    </div>

  );

}
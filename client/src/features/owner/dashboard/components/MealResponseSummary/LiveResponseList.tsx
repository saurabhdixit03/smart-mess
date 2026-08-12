import type { DashboardCustomer } from "../../types/dashboard.types";

type Props = {
  responses: DashboardCustomer[];
};

export default function LiveResponseList({
  responses,
}: Props) {

  const recentResponses =
    responses.slice(0, 5);

  function getRelativeTime(date: string) {

    const now = new Date();

    const respondedAt =
      new Date(date);

    const diff =
      Math.floor(
        (now.getTime() -
          respondedAt.getTime()) /
          1000
      );

    if (diff < 60) {
      return "Just now";
    }

    if (diff < 3600) {
      return `${Math.floor(diff / 60)} min ago`;
    }

    return `${Math.floor(diff / 3600)} hr ago`;

  }

  if (recentResponses.length === 0) {

    return (

      <div className="flex h-full items-center justify-center text-sm text-[var(--color-text-secondary)]">

        Waiting for customer responses...

      </div>

    );

  }

  return (

    <div className="space-y-3">

      <div className="flex items-center justify-between">

        <h4 className="text-sm font-semibold">

          Live Responses

        </h4>

        <div className="flex items-center gap-2 text-xs text-[var(--color-text-secondary)]">

          <span className="h-2 w-2 rounded-full bg-green-500 animate-pulse" />

          Live

        </div>

      </div>

      <div className="space-y-2">

        {recentResponses.map((response) => {

          const accepted =
            response.responseStatus ===
            "ACCEPTED";

          return (

            <div
              key={response.mealResponseId}
              className="flex items-start gap-3 rounded-lg border border-[var(--color-border)] px-2.5 py-2 transition-colors hover:bg-[var(--color-surface-hover)]"
            >

              <span
                className={`mt-1 h-2.5 w-2.5 rounded-full ${
                  accepted
                    ? "bg-green-500"
                    : "bg-red-500"
                }`}
              />

              <div className="min-w-0 flex-1">

                <div className="flex items-center justify-between gap-3">

                  <span className="truncate font-medium">

                    {response.customerName}

                  </span>

                  <span
                    className={`shrink-0 text-xs font-medium ${
                      accepted
                        ? "text-green-600"
                        : "text-red-500"
                    }`}
                  >

                    {accepted
                      ? response.mealOption ===
                        "FULL"
                        ? "Full"
                        : "Half"
                      : "Declined"}

                    {accepted &&
                      response.extraRotiCount >
                        0 &&
                      ` (+${response.extraRotiCount})`}

                  </span>

                </div>

                <p className="mt-0.5 text-xs text-[var(--color-text-secondary)]">

                  {getRelativeTime(
                    response.respondedAt
                  )}

                </p>

              </div>

            </div>

          );

        })}

      </div>

    </div>

  );

}
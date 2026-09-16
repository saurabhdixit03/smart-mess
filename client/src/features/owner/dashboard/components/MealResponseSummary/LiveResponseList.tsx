import type {
  DashboardCustomer,
} from "../../types/dashboard.types";

type LiveResponseListProps = {
  responses: DashboardCustomer[];
};

export default function LiveResponseList({
  responses,
}: LiveResponseListProps) {
  const latestResponses =
    responses.slice(0, 3);

  function getRelativeTime(
    value: string
  ) {
    const respondedAt =
      new Date(value);

    const elapsedSeconds =
      Math.max(
        0,
        Math.floor(
          (Date.now() -
            respondedAt.getTime()) /
            1000
        )
      );

    if (elapsedSeconds < 60) {
      return "Just now";
    }

    const elapsedMinutes =
      Math.floor(
        elapsedSeconds / 60
      );

    if (elapsedMinutes < 60) {
      return `${elapsedMinutes} min ago`;
    }

    const elapsedHours =
      Math.floor(
        elapsedMinutes / 60
      );

    return `${elapsedHours} ${
      elapsedHours === 1
        ? "hr"
        : "hrs"
    } ago`;
  }

  return (
    <section className="h-full">

      <h3 className="text-sm font-semibold text-[var(--color-text)]">
        Latest Responses
      </h3>

      {latestResponses.length === 0 ? (
        <div
          className="
            mt-4
            flex
            min-h-32
            items-center
            justify-center
            rounded-xl
            border
            border-dashed
            border-[var(--color-border)]
            px-4
            text-center
            text-sm
            text-[var(--color-text-secondary)]
          "
        >
          Waiting for customer responses...
        </div>
      ) : (
        <div className="mt-3 space-y-2">

          {latestResponses.map(
            (response) => {
              const accepted =
                response.responseStatus ===
                "ACCEPTED";

              const responseLabel =
                accepted
                  ? response.mealOption ===
                    "FULL"
                    ? "Full"
                    : "Half"
                  : "Declined";

              return (
                <div
                  key={
                    response.mealResponseId
                  }
                  className="
                    flex
                    items-center
                    gap-3
                    rounded-xl
                    border
                    border-[var(--color-border)]
                    px-3
                    py-2.5
                    transition-colors
                    duration-200
                    hover:bg-[var(--color-surface-hover)]
                  "
                >
                  <span
                    className={`
                      h-2.5
                      w-2.5
                      shrink-0
                      rounded-full
                      ${
                        accepted
                          ? "bg-[var(--color-success)]"
                          : "bg-[var(--color-danger)]"
                      }
                    `}
                  />

                  <div className="min-w-0 flex-1">

                    <p className="truncate text-sm font-medium text-[var(--color-text)]">
                      {
                        response.customerName
                      }
                    </p>

                    <p className="mt-0.5 text-xs text-[var(--color-text-secondary)]">
                      {getRelativeTime(
                        response.respondedAt
                      )}
                    </p>

                  </div>

                  <div className="shrink-0 text-right">

                    <p
                      className={`
                        text-xs
                        font-semibold
                        ${
                          accepted
                            ? "text-[var(--color-success)]"
                            : "text-[var(--color-danger)]"
                        }
                      `}
                    >
                      {responseLabel}
                    </p>

                    {accepted &&
                      response.extraRotiCount >
                        0 && (
                        <p className="mt-0.5 text-[11px] text-[var(--color-text-secondary)]">
                          +
                          {
                            response.extraRotiCount
                          }{" "}
                          Roti
                          {response.extraRotiCount >
                          1
                            ? "s"
                            : ""}
                        </p>
                      )}

                  </div>

                </div>
              );
            }
          )}

        </div>
      )}

    </section>
  );
}
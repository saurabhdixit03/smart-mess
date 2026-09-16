import RollingCounter from "@/components/common/business/RollingCounter";

type ResponseProgressProps = {
  totalCustomers: number;
  acceptedCustomers: number;
  declinedCustomers: number;
  pendingCustomers: number;
  respondedCustomers: number;
};

export default function ResponseProgress({
  totalCustomers,
  acceptedCustomers,
  declinedCustomers,
  pendingCustomers,
  respondedCustomers,
}: ResponseProgressProps) {
  const completionPercentage =
    totalCustomers === 0
      ? 0
      : Math.round(
          (respondedCustomers /
            totalCustomers) *
            100
        );

  const acceptedPercentage =
    totalCustomers === 0
      ? 0
      : (acceptedCustomers /
          totalCustomers) *
        100;

  const declinedPercentage =
    totalCustomers === 0
      ? 0
      : (declinedCustomers /
          totalCustomers) *
        100;

  const pendingPercentage =
    totalCustomers === 0
      ? 0
      : (pendingCustomers /
          totalCustomers) *
        100;

  return (
    <section
      className="
        border-t
        border-[var(--color-border)]
        pt-4
      "
    >
      <div
        className="
          flex
          flex-col
          gap-3
          sm:flex-row
          sm:items-center
          sm:justify-between
        "
      >
        <div>

          <h3 className="text-sm font-semibold text-[var(--color-text)]">
            Response Progress
          </h3>

          <p className="mt-1 text-xs text-[var(--color-text-secondary)]">

            <span className="font-semibold text-[var(--color-text)]">
              {respondedCustomers}
            </span>{" "}

            of{" "}

            <span className="font-semibold text-[var(--color-text)]">
              {totalCustomers}
            </span>{" "}

            customers responded

          </p>

        </div>

<div
  className="
    flex
    items-center
    text-2xl
    font-bold
    text-[var(--color-primary)]
  "
>
  <RollingCounter
    value={
      completionPercentage
    }
    compact
  />

  <span>%</span>
</div>
      </div>

      {/* Segmented response progress */}
      <div
        className="
          mt-3
          flex
          h-2.5
          overflow-hidden
          rounded-full
          bg-[var(--color-surface-hover)]
        "
        role="progressbar"
        aria-label="Customer response progress"
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={
          completionPercentage
        }
      >
        <div
          className="
            h-full
            bg-[var(--color-success)]
            transition-all
            duration-700
          "
          style={{
            width: `${acceptedPercentage}%`,
          }}
        />

        <div
          className="
            h-full
            bg-[var(--color-danger)]
            transition-all
            duration-700
          "
          style={{
            width: `${declinedPercentage}%`,
          }}
        />

        <div
          className="
            h-full
            bg-[var(--color-warning)]
            transition-all
            duration-700
          "
          style={{
            width: `${pendingPercentage}%`,
          }}
        />
      </div>

      {/* Segment legend */}
      <div
        className="
          mt-3
          flex
          flex-wrap
          items-center
          gap-x-5
          gap-y-2
          text-xs
          text-[var(--color-text-secondary)]
        "
      >
        <div className="flex items-center gap-2">

          <span className="h-2.5 w-2.5 rounded-full bg-[var(--color-success)]" />

          <span>
            Accepted
          </span>

          <span className="font-semibold text-[var(--color-text)]">
            {acceptedCustomers}
          </span>

        </div>

        <div className="flex items-center gap-2">

          <span className="h-2.5 w-2.5 rounded-full bg-[var(--color-danger)]" />

          <span>
            Declined
          </span>

          <span className="font-semibold text-[var(--color-text)]">
            {declinedCustomers}
          </span>

        </div>

        <div className="flex items-center gap-2">

          <span className="h-2.5 w-2.5 rounded-full bg-[var(--color-warning)]" />

          <span>
            Pending
          </span>

          <span className="font-semibold text-[var(--color-text)]">
            {pendingCustomers}
          </span>

        </div>

      </div>

    </section>
  );
}
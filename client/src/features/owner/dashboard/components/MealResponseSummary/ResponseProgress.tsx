import RollingCounter from "@/components/common/business/RollingCounter";

type Props = {
  totalCustomers: number;
  respondedCustomers: number;
};

export default function ResponseProgress({
  totalCustomers,
  respondedCustomers,
}: Props) {

  const progress =
    totalCustomers === 0
      ? 0
      : Math.round(
          (respondedCustomers /
            totalCustomers) *
            100
        );

  return (

    <div className="border-t border-[var(--color-border)] pt-2">

      <div className="mb-2 flex items-center justify-between">

        <span className="text-sm font-medium">

          Response Progress

        </span>

        <span className="font-semibold text-[var(--color-primary)]">

          <RollingCounter value={progress} />%

        </span>

      </div>

      <div className="h-1 overflow-hidden rounded-full bg-[var(--color-border)]">

        <div
          className="h-full rounded-full bg-[var(--color-primary)] transition-all duration-700"
          style={{
            width: `${progress}%`,
          }}
        />

      </div>

      <div className="mt-1 flex items-center justify-between text-xs text-[var(--color-text-secondary)]">

        <span>

          {respondedCustomers} / {totalCustomers} customers responded

        </span>

        

      </div>

    </div>

  );

}
import Button from "@/components/common/ui/Button/Button";

type InsightsFiltersProps = {
  month: number;
  year: number;
  onMonthChange: (
    month: number
  ) => void;
  onYearChange: (
    year: number
  ) => void;
  onSearch: () => void;
};

const months = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
];

export default function InsightsFilters({
  month,
  year,
  onMonthChange,
  onYearChange,
  onSearch,
}: InsightsFiltersProps) {
  const currentYear =
    new Date().getFullYear();

  return (
    <div
      className="
        interactive-surface
        flex
        flex-wrap
        items-end
        gap-4
        rounded-2xl
        border
        border-[var(--color-border)]
        bg-[var(--color-surface)]
        p-6
      "
    >
      <div>

        <label className="mb-2 block text-sm font-medium">
          Month
        </label>

        <select
          value={month}
          onChange={(event) =>
            onMonthChange(
              Number(
                event.target.value
              )
            )
          }
          className="
            h-11
            rounded-xl
            border
            border-[var(--color-border)]
            bg-[var(--color-surface)]
            px-4
          "
        >
          {months.map(
            (
              monthName,
              index
            ) => (
              <option
                key={monthName}
                value={index + 1}
              >
                {monthName}
              </option>
            )
          )}
        </select>

      </div>

      <div>

        <label className="mb-2 block text-sm font-medium">
          Year
        </label>

        <select
          value={year}
          onChange={(event) =>
            onYearChange(
              Number(
                event.target.value
              )
            )
          }
          className="
            h-11
            rounded-xl
            border
            border-[var(--color-border)]
            bg-[var(--color-surface)]
            px-4
          "
        >
          {Array.from(
            {
              length: 5,
            },
            (_, index) =>
              currentYear - index
          ).map((value) => (
            <option
              key={value}
              value={value}
            >
              {value}
            </option>
          ))}
        </select>

      </div>

      <Button onClick={onSearch}>
        View Insights
      </Button>

    </div>
  );
}
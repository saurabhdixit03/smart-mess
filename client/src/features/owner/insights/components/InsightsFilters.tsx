type InsightsFiltersProps = {
  month: number;
  year: number;
  onMonthChange: (month: number) => void;
  onYearChange: (year: number) => void;
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

const selectClass =
  "h-10 min-w-0 rounded-lg border border-[var(--color-border)] " +
  "bg-[var(--color-surface)] px-3 text-sm text-[var(--color-text)] " +
  "focus-visible:outline-none focus-visible:ring-2 " +
  "focus-visible:ring-[var(--color-primary)]";

export default function InsightsFilters({
  month,
  year,
  onMonthChange,
  onYearChange,
}: InsightsFiltersProps) {
  const currentYear = Number(
    new Intl.DateTimeFormat("en", {
      timeZone: "Asia/Kolkata",
      year: "numeric",
    }).format(new Date())
  );

  const years = Array.from({ length: 5 }, (_, index) => currentYear - index);

  if (!years.includes(year)) {
    years.push(year);
    years.sort((first, second) => second - first);
  }

  return (
    <div className="flex w-full items-center gap-2 sm:w-auto">
      <div className="min-w-0 flex-1 sm:flex-none">
        <label htmlFor="insights-month" className="sr-only">
          Month
        </label>

        <select
          id="insights-month"
          value={month}
          onChange={(event) => onMonthChange(Number(event.target.value))}
          className={`${selectClass} w-full sm:w-36`}
        >
          {months.map((name, index) => (
            <option key={name} value={index + 1}>
              {name}
            </option>
          ))}
        </select>
      </div>

      <div className="shrink-0">
        <label htmlFor="insights-year" className="sr-only">
          Year
        </label>

        <select
          id="insights-year"
          value={year}
          onChange={(event) => onYearChange(Number(event.target.value))}
          className={`${selectClass} w-24`}
        >
          {years.map((value) => (
            <option key={value} value={value}>
              {value}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
}
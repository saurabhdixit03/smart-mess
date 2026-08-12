import Card from "@/components/common/ui/Card/Card";
import Button from "@/components/common/ui/Button/Button";
import Select from "@/components/common/ui/Select/Select";

type GenerateBillCardProps = {
  billingMonth: number;
  billingYear: number;

  onMonthChange: (month: number) => void;
  onYearChange: (year: number) => void;

  onGenerate: () => void;

  loading: boolean;
};

const MONTH_OPTIONS = [
  { label: "January", value: "1" },
  { label: "February", value: "2" },
  { label: "March", value: "3" },
  { label: "April", value: "4" },
  { label: "May", value: "5" },
  { label: "June", value: "6" },
  { label: "July", value: "7" },
  { label: "August", value: "8" },
  { label: "September", value: "9" },
  { label: "October", value: "10" },
  { label: "November", value: "11" },
  { label: "December", value: "12" },
];

const CURRENT_YEAR = new Date().getFullYear();

const YEAR_OPTIONS = Array.from({ length: 5 }, (_, index) => ({
  label: String(CURRENT_YEAR - 2 + index),
  value: String(CURRENT_YEAR - 2 + index),
}));

export default function GenerateBillCard({
  billingMonth,
  billingYear,
  onMonthChange,
  onYearChange,
  onGenerate,
  loading,
}: GenerateBillCardProps) {
  return (
    <Card className="p-6">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end">
        <div className="flex-1">
  <label className="mb-2 block text-sm font-medium text-[var(--color-text)]">
    Billing Month
  </label>

  <Select
    fullWidth
    value={billingMonth}
    onChange={(e) =>
      onMonthChange(Number(e.target.value))
    }
  >
    {MONTH_OPTIONS.map((month) => (
      <option
        key={month.value}
        value={month.value}
      >
        {month.label}
      </option>
    ))}
  </Select>
</div>

<div className="flex-1">
  <label className="mb-2 block text-sm font-medium text-[var(--color-text)]">
    Billing Year
  </label>

  <Select
    fullWidth
    value={billingYear}
    onChange={(e) =>
      onYearChange(Number(e.target.value))
    }
  >
    {YEAR_OPTIONS.map((year) => (
      <option
        key={year.value}
        value={year.value}
      >
        {year.label}
      </option>
    ))}
  </Select>
</div>
        <Button
  onClick={onGenerate}
  disabled={loading}
  className="w-full lg:w-auto"
>
  {loading ? "Generating..." : "Generate Bills"}
</Button>
      </div>
    </Card>
  );
}
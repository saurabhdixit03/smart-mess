type MenuSummaryProps = {
  sabjiOne: string;
  sabjiTwo?: string;
  dal?: string;
  rice?: string;
  sweet?: string;
};

export default function MenuSummary({
  sabjiOne,
  sabjiTwo,
  dal,
  rice,
  sweet,
}: MenuSummaryProps) {
  return (
    <div className="space-y-2">

      <div className="flex flex-wrap gap-2">

        <span className="rounded-full bg-green-100 px-2 py-1 text-xs font-medium text-green-700">
          {sabjiOne}
        </span>

        {sabjiTwo && (
          <span className="rounded-full bg-green-100 px-2 py-1 text-xs font-medium text-green-700">
            {sabjiTwo}
          </span>
        )}

      </div>

      <p className="text-sm text-[var(--color-text-secondary)]">
        {[dal, rice, sweet]
          .filter(Boolean)
          .join(" • ")}
      </p>

    </div>
  );
}
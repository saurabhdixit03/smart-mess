type MenuSummaryProps = {
  sabjiOne: string;
  sabjiTwo?: string | null;
  dal?: string | null;
  rice?: string | null;
  sweet?: string | null;
};

export default function MenuSummary({
  sabjiOne,
  sabjiTwo,
  dal,
  rice,
  sweet,
}: MenuSummaryProps) {
  const sideItems = [
    dal,
    rice,
    sweet,
  ].filter(Boolean);

  return (
    <div className="space-y-4">

      <div className="flex flex-wrap gap-2">

        {[sabjiOne, sabjiTwo]
          .filter(Boolean)
          .map((item) => (
            <span
              key={item}
              className="rounded-full bg-green-100 px-3 py-1 text-xs font-medium text-green-700"
            >
              {item}
            </span>
          ))}

      </div>

      {sideItems.length > 0 && (
        <div className="flex flex-wrap items-center gap-2 text-sm text-[var(--color-text-secondary)]">

          {sideItems.map((item, index) => (
            <div
              key={item}
              className="flex items-center gap-2"
            >
              {index > 0 && (
                <span>•</span>
              )}

              <span>{item}</span>
            </div>
          ))}

        </div>
      )}

    </div>
  );
}
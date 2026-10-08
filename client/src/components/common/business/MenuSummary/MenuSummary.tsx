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
  const sabjis = [sabjiOne, sabjiTwo]
    .filter((item) => item?.trim())
    .join(" · ");

  const sideItems = [dal, rice, sweet]
    .filter((item) => item?.trim())
    .join(" · ");

  return (
    <div className="space-y-2">
      <p className="break-words text-base font-medium leading-6 text-[var(--color-text)]">
        {sabjis}
      </p>

      {sideItems && (
        <p className="break-words text-sm leading-5 text-[var(--color-text-secondary)]">
          {sideItems}
        </p>
      )}
    </div>
  );
}
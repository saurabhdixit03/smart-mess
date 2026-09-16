import RollingDigit from "./RollingDigit";

type RollingCounterProps = {
  value: number;
  compact?: boolean;
};

export default function RollingCounter({
  value,
  compact = false,
}: RollingCounterProps) {
  const digits = value
    .toString()
    .split("")
    .map(Number);

  return (
    <div
      className={`
        flex
        items-center
        justify-center
        ${
          compact
            ? "gap-0"
            : "gap-[2px]"
        }
      `}
    >
      {digits.map(
        (digit, index) => (
          <RollingDigit
            key={index}
            digit={digit}
            compact={compact}
          />
        )
      )}
    </div>
  );
}
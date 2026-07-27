import RollingDigit from "./RollingDigit";

type RollingCounterProps = {
  value: number;
};

export default function RollingCounter({
  value,
}: RollingCounterProps) {
  const digits = value
    .toString()
    .split("")
    .map(Number);

  return (
    <div className="flex items-center justify-center gap-[2px]">
      {digits.map((digit, index) => (
        <RollingDigit
          key={index}
          digit={digit}
        />
      ))}
    </div>
  );
}
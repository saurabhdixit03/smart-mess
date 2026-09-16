import {
  motion,
} from "framer-motion";

import {
  useEffect,
  useRef,
  useState,
} from "react";

type RollingDigitProps = {
  digit: number;
  compact?: boolean;
};

const DIGIT_HEIGHT = 32;

export default function RollingDigit({
  digit,
  compact = false,
}: RollingDigitProps) {
  const previousDigit =
    useRef(digit);

  const [
    offset,
    setOffset,
  ] = useState(
    digit * DIGIT_HEIGHT
  );

  useEffect(() => {
    const from =
      previousDigit.current;

    let to = digit;

    // Always roll forward.
    if (to <= from) {
      to += 10;
    }

    // Add one extra revolution.
    to += 10;

    setOffset(
      to * DIGIT_HEIGHT
    );

    previousDigit.current =
      digit;
  }, [digit]);

  const digits =
    Array.from(
      { length: 100 },
      (_, index) =>
        index % 10
    );

  return (
    <div
      className={`
        relative
        h-8
        overflow-hidden
        ${
          compact
            ? "w-4"
            : "w-5"
        }
      `}
    >
      <motion.div
        animate={{
          y: -offset,
        }}
        transition={{
          type: "spring",
          stiffness: 55,
          damping: 12,
          mass: 1.4,
        }}
        className="flex flex-col"
      >
        {digits.map(
          (value, index) => (
            <div
              key={index}
              className="
                flex
                h-8
                items-center
                justify-center
                font-bold
              "
            >
              {value}
            </div>
          )
        )}
      </motion.div>
    </div>
  );
}
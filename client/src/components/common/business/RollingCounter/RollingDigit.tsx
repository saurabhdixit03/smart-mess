import { motion } from "framer-motion";
import { useEffect, useRef, useState } from "react";

type RollingDigitProps = {
  digit: number;
};

const DIGIT_HEIGHT = 32;

export default function RollingDigit({
  digit,
}: RollingDigitProps) {

  const previousDigit = useRef(digit);

  const [offset, setOffset] = useState(
    digit * DIGIT_HEIGHT
  );

  useEffect(() => {

    let from = previousDigit.current;
let to = digit;

// Always roll forward
if (to <= from) {
  to += 10;
}

// Add one extra revolution.
// Remove the +10 later if you want a shorter roll.
to += 10;

setOffset(to * DIGIT_HEIGHT);

    previousDigit.current = digit;

  }, [digit]);

  const digits = [];

for (let i = 0; i < 100; i++) {
    digits.push(i % 10);
}

  return (

    <div className="relative h-8 w-5 overflow-hidden">

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

        {digits.map((value, index) => (

          <div
            key={index}
            className="flex h-8 items-center justify-center font-bold"
          >
            {value}
          </div>

        ))}

      </motion.div>

    </div>

  );

}
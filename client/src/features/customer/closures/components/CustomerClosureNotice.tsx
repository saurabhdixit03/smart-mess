import {
  CalendarClock,
} from "lucide-react";

import { useCustomerClosure } from "../hooks/useCustomerClosure";

import type { MessClosureResponse } from "@/features/owner/settings/types";

function formatDate(
  value: string
) {
  return new Date(
    `${value}T00:00:00`
  ).toLocaleDateString(
    undefined,
    {
      day: "2-digit",
      month: "short",
    }
  );
}

function formatSession(
  value:
    | MessClosureResponse["startSession"]
    | MessClosureResponse["endSession"]
) {
  return value === "LUNCH"
    ? "Lunch"
    : "Dinner";
}

function buildClosurePeriod(
  closure: MessClosureResponse
) {
  const startDate =
    formatDate(
      closure.startDate
    );

  const endDate =
    formatDate(
      closure.endDate
    );

  if (
    closure.startDate ===
      closure.endDate &&
    closure.startSession ===
      "LUNCH" &&
    closure.endSession ===
      "DINNER"
  ) {
    return `${startDate} · Full day`;
  }

  if (
    closure.startDate ===
      closure.endDate &&
    closure.startSession ===
      closure.endSession
  ) {
    return `${startDate} · ${formatSession(
      closure.startSession
    )}`;
  }

  return `${formatSession(
    closure.startSession
  )} ${startDate} → ${formatSession(
    closure.endSession
  )} ${endDate}`;
}

function isClosureToday(
  closure: MessClosureResponse
) {
  const today =
    new Date();

  const currentDate =
    [
      today.getFullYear(),
      String(
        today.getMonth() + 1
      ).padStart(2, "0"),
      String(
        today.getDate()
      ).padStart(2, "0"),
    ].join("-");

  return (
    closure.startDate <=
      currentDate &&
    closure.endDate >=
      currentDate
  );
}

export default function CustomerClosureNotice() {
  const {
    closure,
    loading,
    error,
  } = useCustomerClosure();

  if (
    loading ||
    error ||
    !closure
  ) {
    return null;
  }

  const activeToday =
    isClosureToday(
      closure
    );

  return (
    <div
      className="
        closure-notice-enter
        rounded-xl
        border
        border-[var(--color-border)]
        bg-[var(--color-surface-hover)]
        p-4
      "
    >
      <div className="flex items-start gap-3">
        <div
          className="
            flex
            h-8
            w-8
            shrink-0
            items-center
            justify-center
            rounded-lg
            bg-[var(--color-primary)]/10
            text-[var(--color-primary)]
          "
        >
          <CalendarClock size={16} />
        </div>

        <div className="min-w-0 flex-1">
          <p
            className="
              text-xs
              font-semibold
              text-[var(--color-text)]
            "
          >
            {activeToday
              ? "Mess Closure"
              : "Upcoming Closure"}
          </p>

          <p
            className="
              mt-1
              text-xs
              font-medium
              text-[var(--color-text)]
            "
          >
            {buildClosurePeriod(
              closure
            )}
          </p>

          <p
            className="
              mt-1
              line-clamp-2
              text-xs
              leading-5
              text-[var(--color-text-secondary)]
            "
          >
            {closure.reason}
          </p>
        </div>
      </div>
    </div>
  );
}
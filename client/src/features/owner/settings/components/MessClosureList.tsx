import {
  CalendarDays,
  Pencil,
  Trash2,
} from "lucide-react";

import Button from "@/components/common/ui/Button/Button";

import type {
  MessClosureResponse,
} from "../types";

type MessClosureListProps = {
  closures: MessClosureResponse[];
  loading?: boolean;
  saving?: boolean;

  onEdit: (
    closure: MessClosureResponse
  ) => void;

  onDelete: (
    closureId: number
  ) => Promise<boolean>;
};

function formatSession(
  session: MessClosureResponse["startSession"]
) {
  return session === "LUNCH"
    ? "Lunch"
    : "Dinner";
}

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
      year: "numeric",
    }
  );
}

export default function MessClosureList({
  closures,
  loading = false,
  saving = false,
  onEdit,
  onDelete,
}: MessClosureListProps) {
  if (loading) {
    return (
      <div
        className="
          py-8
          text-center
          text-sm
          text-[var(--color-text-secondary)]
        "
      >
        Loading temporary closures...
      </div>
    );
  }

  if (closures.length === 0) {
    return (
      <div
        className="
          rounded-[var(--radius-lg)]
          border
          border-dashed
          border-[var(--color-border)]
          px-5
          py-8
          text-center
        "
      >
        <CalendarDays
          size={26}
          className="
            mx-auto
            mb-3
            text-[var(--color-text-secondary)]
          "
        />

        <p
          className="
            text-sm
            font-medium
            text-[var(--color-text)]
          "
        >
          No current or upcoming closures
        </p>

        <p
          className="
            mt-1
            text-xs
            text-[var(--color-text-secondary)]
          "
        >
          Scheduled temporary closures will appear here.
        </p>
      </div>
    );
  }

  const sortedClosures = [
    ...closures,
  ].sort((a, b) => {
    const dateComparison =
      a.startDate.localeCompare(
        b.startDate
      );

    if (dateComparison !== 0) {
      return dateComparison;
    }

    const sessionOrder = {
      LUNCH: 0,
      DINNER: 1,
    };

    return (
      sessionOrder[a.startSession] -
      sessionOrder[b.startSession]
    );
  });

  return (
    <div className="space-y-3">
      {sortedClosures.map(
        (closure) => (
          <div
            key={closure.closureId}
            className="
              rounded-[var(--radius-lg)]
              border
              border-[var(--color-border)]
              bg-[var(--color-surface)]
              px-4
              py-3.5
            "
          >
            <div
              className="
                flex
                flex-col
                gap-3
                sm:flex-row
                sm:items-center
                sm:justify-between
              "
            >
              <div className="min-w-0">
                <div
                  className="
                    flex
                    flex-wrap
                    items-center
                    gap-2
                  "
                >
                  <span
                    className="
                      rounded-full
                      bg-[var(--color-primary)]/10
                      px-2.5
                      py-1
                      text-xs
                      font-semibold
                      text-[var(--color-primary)]
                    "
                  >
                    Temporary Closure
                  </span>

                  <p
                    className="
                      text-sm
                      font-semibold
                      text-[var(--color-text)]
                    "
                  >
                    {formatDate(
                      closure.startDate
                    )}{" "}
                    ·{" "}
                    {formatSession(
                      closure.startSession
                    )}
                    {" → "}
                    {formatDate(
                      closure.endDate
                    )}{" "}
                    ·{" "}
                    {formatSession(
                      closure.endSession
                    )}
                  </p>
                </div>

                <p
                  className="
                    mt-2
                    max-w-3xl
                    truncate
                    text-sm
                    text-[var(--color-text-secondary)]
                  "
                  title={closure.reason}
                >
                  {closure.reason}
                </p>
              </div>

              <div
                className="
                  flex
                  shrink-0
                  gap-2
                "
              >
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  disabled={saving}
                  onClick={() =>
                    onEdit(closure)
                  }
                >
                  <Pencil size={14} />
                  Edit
                </Button>

                <Button
                  type="button"
                  variant="danger"
                  size="sm"
                  disabled={saving}
                  onClick={() =>
                    onDelete(
                      closure.closureId
                    )
                  }
                >
                  <Trash2 size={14} />
                  Delete
                </Button>
              </div>
            </div>
          </div>
        )
      )}
    </div>
  );
}
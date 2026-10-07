import {
  useRef,
  useState,
  type FormEvent,
} from "react";

import Button from "@/components/common/ui/Button/Button";
import Input from "@/components/common/ui/Input/Input";
import Select from "@/components/common/ui/Select/Select";
import Textarea from "@/components/common/ui/Textarea/Textarea";

import type {
  CreateMessClosureRequest,
  MealSession,
  MessClosureResponse,
  UpdateMessClosureRequest,
} from "../types";

type MessClosureFormProps = {
  closure?: MessClosureResponse | null;
  saving?: boolean;

  onCreate: (
    payload: CreateMessClosureRequest
  ) => Promise<boolean>;

  onUpdate: (
    closureId: number,
    payload: UpdateMessClosureRequest
  ) => Promise<boolean>;

  onCancelEdit?: () => void;
};

function getTodayDate(): string {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Kolkata",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(new Date());

  const year = parts.find(
    (part) => part.type === "year"
  )!.value;

  const month = parts.find(
    (part) => part.type === "month"
  )!.value;

  const day = parts.find(
    (part) => part.type === "day"
  )!.value;

  return `${year}-${month}-${day}`;
}

function isValidDate(value: string): boolean {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return false;
  }

  const date = new Date(`${value}T00:00:00Z`);

  return (
    !Number.isNaN(date.getTime()) &&
    date.toISOString().slice(0, 10) === value
  );
}

export default function MessClosureForm(
  props: MessClosureFormProps
) {
  const closure = props.closure;

  const formKey = JSON.stringify([
    closure?.closureId ?? null,
    closure?.startDate ?? null,
    closure?.startSession ?? null,
    closure?.endDate ?? null,
    closure?.endSession ?? null,
    closure?.reason ?? null,
  ]);

  return (
    <MessClosureFormContent
      key={formKey}
      {...props}
    />
  );
}

function MessClosureFormContent({
  closure,
  saving = false,
  onCreate,
  onUpdate,
  onCancelEdit,
}: MessClosureFormProps) {
  const isEditMode = closure != null;

  const [startDate, setStartDate] =
    useState(closure?.startDate ?? "");

  const [startSession, setStartSession] =
    useState<MealSession>(
      closure?.startSession ?? "LUNCH"
    );

  const [endDate, setEndDate] =
    useState(closure?.endDate ?? "");

  const [endSession, setEndSession] =
    useState<MealSession>(
      closure?.endSession ?? "DINNER"
    );

  const [reason, setReason] =
    useState(closure?.reason ?? "");

  const [validationError, setValidationError] =
    useState<string | null>(null);

  const [submitting, setSubmitting] = useState(false);
  const submittingRef = useRef(false);

  const busy = saving || submitting;
  const today = getTodayDate();

  function clearError() {
    setValidationError(null);
  }

  function validateForm(): string | null {
    if (!isValidDate(startDate)) {
      return "Select a valid start date.";
    }

    if (!isValidDate(endDate)) {
      return "Select a valid end date.";
    }

    if (startDate < getTodayDate()) {
      return "Closure start date cannot be in the past.";
    }

    if (endDate < startDate) {
      return "Closure end date cannot be before the start date.";
    }

    if (
      startDate === endDate &&
      startSession === "DINNER" &&
      endSession === "LUNCH"
    ) {
      return "For a single-day closure, the ending meal cannot be before the starting meal.";
    }

    if (!reason.trim()) {
      return "Enter a reason for the closure.";
    }

    if (reason.trim().length > 255) {
      return "Closure reason must not exceed 255 characters.";
    }

    return null;
  }

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    if (saving || submittingRef.current) {
      return;
    }

    const message = validateForm();

    setValidationError(message);

    if (message) {
      return;
    }

    const payload: CreateMessClosureRequest = {
      startDate,
      startSession,
      endDate,
      endSession,
      reason: reason.trim(),
    };

    submittingRef.current = true;
    setSubmitting(true);

    try {
      const success = closure
        ? await onUpdate(closure.closureId, payload)
        : await onCreate(payload);

      if (success && !isEditMode) {
        setStartDate("");
        setStartSession("LUNCH");
        setEndDate("");
        setEndSession("DINNER");
        setReason("");
      }
    } catch (error: unknown) {
      setValidationError(
        error instanceof Error
          ? error.message
          : "Unable to save the closure."
      );
    } finally {
      submittingRef.current = false;
      setSubmitting(false);
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-5"
    >
      <fieldset
        disabled={busy}
        className="space-y-5"
      >
        <legend className="sr-only">
          Temporary mess closure
        </legend>

        <div className="grid gap-4 sm:grid-cols-2">
          <section className="min-w-0 rounded-xl border border-[var(--color-border)] p-4">
            <h3 className="mb-4 text-sm font-semibold">
              Closure Starts
            </h3>

            <div className="space-y-3">
              <div>
                <label
                  htmlFor="closure-start-date"
                  className="mb-1.5 block text-xs font-medium text-[var(--color-text-secondary)]"
                >
                  Date
                </label>

                <Input
                  id="closure-start-date"
                  fullWidth
                  required
                  type="date"
                  min={today}
                  value={startDate}
                  onChange={(event) => {
                    setStartDate(event.target.value);
                    clearError();
                  }}
                />
              </div>

              <div>
                <label
                  htmlFor="closure-start-session"
                  className="mb-1.5 block text-xs font-medium text-[var(--color-text-secondary)]"
                >
                  From Meal
                </label>

                <Select
                  id="closure-start-session"
                  className="w-full"
                  value={startSession}
                  onChange={(event) => {
                    setStartSession(
                      event.target.value as MealSession
                    );
                    clearError();
                  }}
                >
                  <option value="LUNCH">Lunch</option>
                  <option value="DINNER">Dinner</option>
                </Select>
              </div>
            </div>
          </section>

          <section className="min-w-0 rounded-xl border border-[var(--color-border)] p-4">
            <h3 className="mb-4 text-sm font-semibold">
              Closure Ends
            </h3>

            <div className="space-y-3">
              <div>
                <label
                  htmlFor="closure-end-date"
                  className="mb-1.5 block text-xs font-medium text-[var(--color-text-secondary)]"
                >
                  Date
                </label>

                <Input
                  id="closure-end-date"
                  fullWidth
                  required
                  type="date"
                  min={
                    startDate && startDate > today
                      ? startDate
                      : today
                  }
                  value={endDate}
                  onChange={(event) => {
                    setEndDate(event.target.value);
                    clearError();
                  }}
                />
              </div>

              <div>
                <label
                  htmlFor="closure-end-session"
                  className="mb-1.5 block text-xs font-medium text-[var(--color-text-secondary)]"
                >
                  Through Meal
                </label>

                <Select
                  id="closure-end-session"
                  className="w-full"
                  value={endSession}
                  onChange={(event) => {
                    setEndSession(
                      event.target.value as MealSession
                    );
                    clearError();
                  }}
                >
                  <option value="LUNCH">Lunch</option>
                  <option value="DINNER">Dinner</option>
                </Select>
              </div>
            </div>
          </section>
        </div>

        <div>
          <label
            htmlFor="closure-reason"
            className="mb-2 block text-sm font-medium"
          >
            Reason
          </label>

          <Textarea
            id="closure-reason"
            required
            className="w-full"
            value={reason}
            maxLength={255}
            rows={3}
            placeholder="For example, festival holiday or maintenance."
            onChange={(event) => {
              setReason(event.target.value);
              clearError();
            }}
          />

          <p className="mt-1 text-right text-xs text-[var(--color-text-secondary)]">
            {reason.length}/255
          </p>
        </div>
      </fieldset>

      <p className="text-xs leading-5 text-[var(--color-text-secondary)]">
        The selected starting and ending meals are included.
        If the starting meal already has a published menu,
        the closure begins from the next available meal session.
      </p>

      {validationError && (
        <p
          role="alert"
          className="text-sm text-red-500"
        >
          {validationError}
        </p>
      )}

      <div className="flex justify-end gap-3 border-t border-[var(--color-border)] pt-4">
        {onCancelEdit && (
          <Button
            type="button"
            variant="secondary"
            disabled={busy}
            onClick={onCancelEdit}
          >
            Cancel
          </Button>
        )}

        <Button
          type="submit"
          disabled={busy}
        >
          {busy
            ? "Saving..."
            : isEditMode
              ? "Save Changes"
              : "Schedule Closure"}
        </Button>
      </div>
    </form>
  );
}
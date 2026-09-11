import {
  useEffect,
  useMemo,
  useState,
} from "react";

import { toast } from "sonner";

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

const sessionOptions: {
  label: string;
  value: MealSession;
}[] = [
  {
    label: "Lunch",
    value: "LUNCH",
  },
  {
    label: "Dinner",
    value: "DINNER",
  },
];

function getTodayDate() {
  return new Date()
    .toISOString()
    .slice(0, 10);
}

export default function MessClosureForm({
  closure,
  saving = false,
  onCreate,
  onUpdate,
  onCancelEdit,
}: MessClosureFormProps) {
  const isEditMode =
    closure != null;

  const [startDate, setStartDate] =
    useState("");

  const [startSession, setStartSession] =
    useState<MealSession>("LUNCH");

  const [endDate, setEndDate] =
    useState("");

  const [endSession, setEndSession] =
    useState<MealSession>("DINNER");

  const [reason, setReason] =
    useState("");

  const today =
    useMemo(
      () => getTodayDate(),
      []
    );

  useEffect(() => {
    if (!closure) {
      setStartDate("");
      setStartSession("LUNCH");
      setEndDate("");
      setEndSession("DINNER");
      setReason("");

      return;
    }

    setStartDate(
      closure.startDate
    );

    setStartSession(
      closure.startSession
    );

    setEndDate(
      closure.endDate
    );

    setEndSession(
      closure.endSession
    );

    setReason(
      closure.reason
    );
  }, [closure]);

  function validateForm() {
    if (!startDate) {
      toast.error(
        "Start date is required."
      );

      return false;
    }

    if (!endDate) {
      toast.error(
        "End date is required."
      );

      return false;
    }

    if (startDate < today) {
      toast.error(
        "Closure start date cannot be in the past."
      );

      return false;
    }

    if (endDate < startDate) {
      toast.error(
        "Closure end date cannot be before the start date."
      );

      return false;
    }

    if (
      startDate === endDate &&
      startSession === "DINNER" &&
      endSession === "LUNCH"
    ) {
      toast.error(
        "Closure end session cannot be before the start session on the same date."
      );

      return false;
    }

    const trimmedReason =
      reason.trim();

    if (!trimmedReason) {
      toast.error(
        "Closure reason is required."
      );

      return false;
    }

    if (
      trimmedReason.length > 255
    ) {
      toast.error(
        "Closure reason must not exceed 255 characters."
      );

      return false;
    }

    return true;
  }

  async function handleSubmit(
    e: React.FormEvent
  ) {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    const payload = {
      startDate,
      startSession,
      endDate,
      endSession,
      reason: reason.trim(),
    };

    let success = false;

    if (
      isEditMode &&
      closure
    ) {
      success =
        await onUpdate(
          closure.closureId,
          payload
        );
    } else {
      success =
        await onCreate(
          payload
        );
    }

    if (
      success &&
      !isEditMode
    ) {
      setStartDate("");
      setStartSession("LUNCH");
      setEndDate("");
      setEndSession("DINNER");
      setReason("");
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-5"
    >
      <div
        className="
          grid
          gap-4
          md:grid-cols-2
        "
      >
        <div>
          <label className="mb-2 block text-sm font-medium">
            Start Date
          </label>

          <Input
            fullWidth
            type="date"
            min={today}
            value={startDate}
            onChange={(e) =>
              setStartDate(
                e.target.value
              )
            }
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium">
            Start Session
          </label>

          <Select
            value={startSession}
            onChange={(e) =>
              setStartSession(
                e.target
                  .value as MealSession
              )
            }
          >
            {sessionOptions.map(
              (option) => (
                <option
                  key={option.value}
                  value={option.value}
                >
                  {option.label}
                </option>
              )
            )}
          </Select>
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium">
            End Date
          </label>

          <Input
            fullWidth
            type="date"
            min={
              startDate || today
            }
            value={endDate}
            onChange={(e) =>
              setEndDate(
                e.target.value
              )
            }
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium">
            End Session
          </label>

          <Select
            value={endSession}
            onChange={(e) =>
              setEndSession(
                e.target
                  .value as MealSession
              )
            }
          >
            {sessionOptions.map(
              (option) => (
                <option
                  key={option.value}
                  value={option.value}
                >
                  {option.label}
                </option>
              )
            )}
          </Select>
        </div>
      </div>

      <div>
        <label className="mb-2 block text-sm font-medium">
          Closure Reason
        </label>

        <Textarea
          value={reason}
          maxLength={255}
          rows={3}
          placeholder="e.g. Festival holiday, maintenance, emergency closure..."
          onChange={(e) =>
            setReason(
              e.target.value
            )
          }
        />

        <div
          className="
            mt-1
            text-right
            text-xs
            text-[var(--color-text-secondary)]
          "
        >
          {reason.length}/255
        </div>
      </div>

      <p className="text-xs leading-5 text-[var(--color-text-secondary)]">
        If a menu is already published for the selected starting session,
        the closure will begin from the next available meal session.
      </p>

      <div className="flex justify-end gap-3">
        {isEditMode && (
          <Button
            type="button"
            variant="secondary"
            disabled={saving}
            onClick={
              onCancelEdit
            }
          >
            Cancel
          </Button>
        )}

        <Button
          type="submit"
          disabled={saving}
        >
          {saving
            ? "Saving..."
            : isEditMode
              ? "Update Closure"
              : "Schedule Closure"}
        </Button>
      </div>
    </form>
  );
}
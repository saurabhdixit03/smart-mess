import {
  useState,
  type FormEvent,
} from "react";

import { toast } from "sonner";

import Button from "@/components/common/ui/Button/Button";
import Select from "@/components/common/ui/Select/Select";

import { settingsApi } from "../api";

import type {
  DayOfWeek,
  MessSettingsResponse,
  UpdateWeeklyScheduleRequest,
} from "../types";

type WeeklyScheduleFormProps = {
  settings: MessSettingsResponse | null;
  onSuccess: () => Promise<void>;
};

const dayOptions = [
  { label: "No Weekly Off", value: "" },
  { label: "Monday", value: "MONDAY" },
  { label: "Tuesday", value: "TUESDAY" },
  { label: "Wednesday", value: "WEDNESDAY" },
  { label: "Thursday", value: "THURSDAY" },
  { label: "Friday", value: "FRIDAY" },
  { label: "Saturday", value: "SATURDAY" },
  { label: "Sunday", value: "SUNDAY" },
];

export default function WeeklyScheduleForm(
  props: WeeklyScheduleFormProps
) {
  const formKey = JSON.stringify([
    props.settings?.settingsId ?? null,
    props.settings?.weeklyClosedDay ?? null,
    props.settings?.weeklyLunchClosed ?? false,
    props.settings?.weeklyDinnerClosed ?? false,
  ]);

  return (
    <WeeklyScheduleFormContent
      key={formKey}
      {...props}
    />
  );
}

function WeeklyScheduleFormContent({
  settings,
  onSuccess,
}: WeeklyScheduleFormProps) {
  const [weeklyClosedDay, setWeeklyClosedDay] =
    useState<DayOfWeek | "">(
      settings?.weeklyClosedDay ?? ""
    );

  const [weeklyLunchClosed, setWeeklyLunchClosed] =
    useState(
      settings?.weeklyLunchClosed ?? false
    );

  const [weeklyDinnerClosed, setWeeklyDinnerClosed] =
    useState(
      settings?.weeklyDinnerClosed ?? false
    );

  const [saving, setSaving] =
    useState(false);

  async function handleSubmit(
    e: FormEvent<HTMLFormElement>
  ) {
    e.preventDefault();

    if (
      weeklyClosedDay &&
      !weeklyLunchClosed &&
      !weeklyDinnerClosed
    ) {
      toast.error(
        "Select at least one closed meal session."
      );

      return;
    }

    if (
      !weeklyClosedDay &&
      (weeklyLunchClosed ||
        weeklyDinnerClosed)
    ) {
      toast.error(
        "Select a weekly closed day first."
      );

      return;
    }

    try {
      setSaving(true);

      const payload: UpdateWeeklyScheduleRequest = {
        weeklyClosedDay:
          weeklyClosedDay || null,
        weeklyLunchClosed,
        weeklyDinnerClosed,
      };

      await settingsApi.updateWeeklySchedule(
        payload
      );

      toast.success(
        "Weekly schedule updated successfully."
      );

      await onSuccess();
    } catch (error) {
      console.error(error);

      toast.error(
        "Failed to update weekly schedule."
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-6"
    >
      <div>
        <label className="mb-2 block text-sm font-medium">
          Weekly Closed Day
        </label>

        <Select
          value={weeklyClosedDay}
          onChange={(e) =>
            setWeeklyClosedDay(
              e.target.value as DayOfWeek | ""
            )
          }
        >
          {dayOptions.map((option) => (
            <option
              key={option.value}
              value={option.value}
            >
              {option.label}
            </option>
          ))}
        </Select>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <label
          className="
            flex
            cursor-pointer
            items-center
            gap-3
            rounded-[var(--radius-md)]
            border
            border-[var(--color-border)]
            p-4
          "
        >
          <input
            type="checkbox"
            checked={weeklyLunchClosed}
            disabled={!weeklyClosedDay}
            onChange={(e) =>
              setWeeklyLunchClosed(
                e.target.checked
              )
            }
          />

          <div>
            <p className="text-sm font-medium">
              Lunch Closed
            </p>

            <p className="text-xs text-[var(--color-text-secondary)]">
              Lunch will not operate on the selected day.
            </p>
          </div>
        </label>

        <label
          className="
            flex
            cursor-pointer
            items-center
            gap-3
            rounded-[var(--radius-md)]
            border
            border-[var(--color-border)]
            p-4
          "
        >
          <input
            type="checkbox"
            checked={weeklyDinnerClosed}
            disabled={!weeklyClosedDay}
            onChange={(e) =>
              setWeeklyDinnerClosed(
                e.target.checked
              )
            }
          />

          <div>
            <p className="text-sm font-medium">
              Dinner Closed
            </p>

            <p className="text-xs text-[var(--color-text-secondary)]">
              Dinner will not operate on the selected day.
            </p>
          </div>
        </label>
      </div>

      <div className="flex justify-end">
        <Button
          type="submit"
          disabled={saving}
        >
          {saving
            ? "Updating..."
            : "Update Weekly Schedule"}
        </Button>
      </div>
    </form>
  );
}
import {
  useEffect,
  useState,
} from "react";

import { toast } from "sonner";

import Button from "@/components/common/ui/Button/Button";
import Input from "@/components/common/ui/Input/Input";

import { settingsApi } from "../api";

import type {
  MessSettingsResponse,
  UpdateResponseWindowRequest,
} from "../types";

type ResponseWindowFormProps = {
  settings: MessSettingsResponse | null;
  onSuccess: () => Promise<void>;
};

export default function ResponseWindowForm({
  settings,
  onSuccess,
}: ResponseWindowFormProps) {
  const [lunchResponseCutoff, setLunchResponseCutoff] =
    useState("");

  const [dinnerResponseCutoff, setDinnerResponseCutoff] =
    useState("");

  const [saving, setSaving] =
    useState(false);

  useEffect(() => {
    if (!settings) {
      return;
    }

    setLunchResponseCutoff(
      settings.lunchResponseCutoff
        ? settings.lunchResponseCutoff.slice(0, 5)
        : ""
    );

    setDinnerResponseCutoff(
      settings.dinnerResponseCutoff
        ? settings.dinnerResponseCutoff.slice(0, 5)
        : ""
    );
  }, [settings]);

  const handleSubmit = async (
    e: React.FormEvent
  ) => {
    e.preventDefault();

    if (
      !lunchResponseCutoff ||
      !dinnerResponseCutoff
    ) {
      toast.error(
        "Both response cutoff times are required."
      );

      return;
    }

    try {
      setSaving(true);

      const payload:
        UpdateResponseWindowRequest = {
          lunchResponseCutoff,
          dinnerResponseCutoff,
        };

      await settingsApi.updateResponseWindow(
        payload
      );

      toast.success(
        "Response window updated successfully."
      );

      await onSuccess();
    } catch (error) {
      console.error(error);

      toast.error(
        "Failed to update response window."
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-6"
    >
      <div
        className="
          grid
          gap-5
          md:grid-cols-2
        "
      >
        <div>
          <label className="mb-2 block text-sm font-medium">
            Lunch Response Cutoff
          </label>

          <Input
            fullWidth
            type="time"
            value={lunchResponseCutoff}
            onChange={(e) =>
              setLunchResponseCutoff(
                e.target.value
              )
            }
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium">
            Dinner Response Cutoff
          </label>

          <Input
            fullWidth
            type="time"
            value={dinnerResponseCutoff}
            onChange={(e) =>
              setDinnerResponseCutoff(
                e.target.value
              )
            }
          />
        </div>
      </div>

      <p className="text-sm text-[var(--color-text-secondary)]">
        Customers can submit or update their meal response only until the configured cutoff time for each session.
      </p>

      <div className="flex justify-end">
        <Button
          type="submit"
          disabled={saving}
        >
          {saving
            ? "Updating..."
            : "Update Response Window"}
        </Button>
      </div>
    </form>
  );
}
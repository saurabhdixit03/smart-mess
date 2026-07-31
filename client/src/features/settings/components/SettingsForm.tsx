import { useEffect, useState } from "react";

import { toast } from "sonner";

import Button from "@/components/common/ui/Button/Button";
import Input from "@/components/common/ui/Input/Input";

import { settingsApi } from "../api";

import type {
  CreateMessSettingsRequest,
  MessSettingsResponse,
  UpdateMessSettingsRequest,
} from "../types";

type SettingsFormProps = {
  settings: MessSettingsResponse | null;
  onSuccess: () => Promise<void>;
};

export default function SettingsForm({
  settings,
  onSuccess,
}: SettingsFormProps) {

  const [upiId, setUpiId] =
    useState("");

  const [receiverName, setReceiverName] =
    useState("");

  const [saving, setSaving] =
    useState(false);

  useEffect(() => {

    if (settings) {

      setUpiId(settings.upiId);

      setReceiverName(
        settings.receiverName
      );

    }

  }, [settings]);

  const handleSubmit =
    async (
      e: React.FormEvent
    ) => {

      e.preventDefault();

      try {

        setSaving(true);

        if (settings === null) {

          const payload: CreateMessSettingsRequest = {
            upiId,
            receiverName,
          };

          await settingsApi.createSettings(
            payload
          );

          toast.success(
            "Settings created successfully."
          );

        } else {

          const payload: UpdateMessSettingsRequest = {
            upiId,
            receiverName,
          };

          await settingsApi.updateSettings(
            payload
          );

          toast.success(
            "Settings updated successfully."
          );

        }

        await onSuccess();

      } catch (error) {

        console.error(error);

        toast.error(
          "Failed to save settings."
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

      <div>

        <label className="mb-2 block text-sm font-medium">

          UPI ID

        </label>

        <Input
          fullWidth
          placeholder="example@upi"
          value={upiId}
          onChange={(e) =>
            setUpiId(e.target.value)
          }
        />

      </div>

      <div>

        <label className="mb-2 block text-sm font-medium">

          Receiver Name

        </label>

        <Input
          fullWidth
          placeholder="Receiver Name"
          value={receiverName}
          onChange={(e) =>
            setReceiverName(
              e.target.value
            )
          }
        />

      </div>

      <div className="flex justify-end">

        <Button
          type="submit"
          disabled={saving}
        >

          {saving
            ? "Saving..."
            : settings === null
            ? "Create Settings"
            : "Update Settings"}

        </Button>

      </div>

    </form>

  );

}
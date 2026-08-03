import { useEffect, useState } from "react";

import {
  Button,
  Input,
  Modal,
} from "@/components/common/ui";

import { useUpdateProfile } from "../hooks";

import type {
  CustomerProfile,
  UpdateProfileRequest,
} from "../types";

interface EditProfileModalProps {
  open: boolean;

  profile: CustomerProfile | null;

  onClose: () => void;

  onUpdated: (
    profile: CustomerProfile
  ) => void;
}

export default function EditProfileModal({
  open,
  profile,
  onClose,
  onUpdated,
}: EditProfileModalProps) {
  const {
    updateProfile,
    loading,
    error,
  } = useUpdateProfile();

  const [form, setForm] =
    useState<UpdateProfileRequest>({
      fullName: "",
      mobileNumber: "",
      email: "",
    });

  useEffect(() => {
    if (!profile) {
      return;
    }

    setForm({
      fullName: profile.fullName,
      mobileNumber: profile.mobileNumber,
      email: profile.email ?? "",
    });
  }, [profile]);

  function handleChange(
    field: keyof UpdateProfileRequest,
    value: string
  ) {
    setForm((previous) => ({
      ...previous,
      [field]: value,
    }));
  }

  async function handleSubmit() {
    if (!profile) {
      return;
    }

    const updated =
      await updateProfile(
        profile.customerId,
        form
      );

    if (updated) {
      onUpdated(updated);

      onClose();
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Edit Profile"
      size="lg"
      footer={
        <>
          <Button
            variant="secondary"
            onClick={onClose}
          >
            Cancel
          </Button>

          <Button
            disabled={loading}
            onClick={handleSubmit}
          >
            {loading
              ? "Saving..."
              : "Save Changes"}
          </Button>
        </>
      }
    >
      <div className="space-y-6">

        <div>
          <h3 className="text-base font-semibold">
            Personal Information
          </h3>

          <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
            Update your contact information.
          </p>
        </div>

        <div className="grid gap-5 md:grid-cols-2">

          <div className="space-y-2">

            <label className="text-sm font-medium">
              Full Name
            </label>

            <Input
              fullWidth
              required
              value={form.fullName}
              onChange={(e) =>
                handleChange(
                  "fullName",
                  e.target.value
                )
              }
            />

          </div>

          <div className="space-y-2">

            <label className="text-sm font-medium">
              Mobile Number
            </label>

            <Input
              fullWidth
              required
              maxLength={10}
              value={form.mobileNumber}
              onChange={(e) =>
                handleChange(
                  "mobileNumber",
                  e.target.value
                )
              }
            />

          </div>

        </div>

        <div className="space-y-2">

          <label className="text-sm font-medium">
            Email Address
          </label>

          <Input
            fullWidth
            type="email"
            value={form.email}
            onChange={(e) =>
              handleChange(
                "email",
                e.target.value
              )
            }
          />

        </div>

        {error && (
          <p className="text-sm text-red-500">
            {error}
          </p>
        )}

      </div>
    </Modal>
  );
}
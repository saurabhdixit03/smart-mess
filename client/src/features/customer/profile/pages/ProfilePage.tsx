import { useState } from "react";

import { PageHeader } from "@/components/common/ui";

import {
  EditProfileModal,
  ProfileCard,
} from "../components";

import { useProfile } from "../hooks";

import type { CustomerProfile } from "../types";

export default function ProfilePage() {
  /*
   * Temporary until authentication is implemented.
   */
  const customerId = 3;

  const {
    profile,
    loading,
    error,
    fetchProfile,
  } = useProfile(customerId);

  const [modalOpen, setModalOpen] =
    useState(false);

  const [currentProfile, setCurrentProfile] =
    useState<CustomerProfile | null>(null);

  function handleEdit() {
    setCurrentProfile(profile);

    setModalOpen(true);
  }

  function handleUpdated(
    updatedProfile: CustomerProfile
  ) {
    setCurrentProfile(updatedProfile);

    fetchProfile();

    setModalOpen(false);
  }

  if (loading) {
    return (
      <div className="py-20 text-center text-[var(--color-text-secondary)]">
        Loading profile...
      </div>
    );
  }

  if (error) {
    return (
      <div className="py-20 text-center text-red-500">
        {error}
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="py-20 text-center text-[var(--color-text-secondary)]">
        Profile not found.
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <PageHeader
        title="My Profile"
        description="View and update your account information."
      />

      <ProfileCard
        profile={profile}
        onEdit={handleEdit}
      />

      <EditProfileModal
        open={modalOpen}
        profile={currentProfile}
        onClose={() =>
          setModalOpen(false)
        }
        onUpdated={handleUpdated}
      />
    </div>
  );
}
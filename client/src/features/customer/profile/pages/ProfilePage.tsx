import { PageHeader } from "@/components/common/ui";

import { ProfileCard } from "../components";

import { useProfile } from "../hooks";

import { getCustomer } from "@/features/auth/utils/auth.utils";

export default function ProfilePage() {
  const customer = getCustomer();

  if (!customer) {
    return (
      <div className="py-20 text-center text-red-500">
        Customer session not found.
      </div>
    );
  }

  const {
    profile,
    loading,
    error,
  } = useProfile(customer.customerId);

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
        description="View your account information."
      />

      <ProfileCard profile={profile} />

    </div>
  );
}
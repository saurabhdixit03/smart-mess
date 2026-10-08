import {
  Card,
  PageHeader,
} from "@/components/common/ui";

import { ProfileCard } from "../components";
import { useProfile } from "../hooks";

import { getCustomer } from "@/features/auth/utils/auth.utils";

export default function ProfilePage() {
  const customer = getCustomer();

  if (!customer) {
    return (
      <section className="space-y-4">
        <PageHeader
          title="My Profile"
          description="View your account information."
        />

        <ProfileMessage
          message="Customer session not found."
          error
        />
      </section>
    );
  }

  return (
    <CustomerProfileContent
      key={customer.customerId}
      customerId={customer.customerId}
    />
  );
}

function ProfileMessage({
  message,
  error = false,
}: {
  message: string;
  error?: boolean;
}) {
  return (
    <Card className="w-full max-w-lg">
      <Card.Body className="py-10 text-center">
        <p
          role={error ? "alert" : "status"}
          className={`text-sm ${
            error
              ? "text-[var(--color-danger)]"
              : "text-[var(--color-text-secondary)]"
          }`}
        >
          {message}
        </p>
      </Card.Body>
    </Card>
  );
}

function CustomerProfileContent({
  customerId,
}: {
  customerId: number;
}) {
  const {
    profile,
    loading,
    error,
  } = useProfile(customerId);

  return (
    <section className="min-w-0 space-y-4">
      <PageHeader
        title="My Profile"
        description="View your account information."
      />

      {loading ? (
        <ProfileMessage message="Loading profile..." />
      ) : error ? (
        <ProfileMessage message={error} error />
      ) : !profile ? (
        <ProfileMessage message="Profile not found." />
      ) : (
        <ProfileCard profile={profile} />
      )}
    </section>
  );
}
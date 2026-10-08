import {
  Calendar,
  Mail,
  Phone,
  UserRound,
} from "lucide-react";

import {
  Card,
  StatusBadge,
} from "@/components/common/ui";

import type { CustomerProfile } from "../types";

interface ProfileCardProps {
  profile: CustomerProfile;
}

function formatDate(value: string): string {
  const date = new Date(
    `${value.slice(0, 10)}T00:00:00`
  );

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return date.toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

export default function ProfileCard({
  profile,
}: ProfileCardProps) {
  const status = String(profile.status);

  const statusLabel =
    status.charAt(0).toUpperCase() +
    status.slice(1).toLowerCase();

  return (
    <Card className="interactive-surface w-full max-w-lg">
      <Card.Body className="p-4">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-[var(--color-border)] pb-3">
          <div className="flex min-w-0 flex-1 items-center gap-3">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[var(--color-primary)]/10 text-[var(--color-primary)]">
              <UserRound
                size={20}
                aria-hidden="true"
              />
            </div>

            <h2 className="min-w-0 break-words text-lg font-semibold tracking-tight text-[var(--color-text)]">
              {profile.fullName}
            </h2>
          </div>

          <StatusBadge
            label={statusLabel}
            variant={
              status === "ACTIVE"
                ? "success"
                : status === "INACTIVE"
                  ? "danger"
                  : "warning"
            }
          />
        </div>

        <dl className="mt-3 divide-y divide-[var(--color-border)] rounded-xl border border-[var(--color-border)] bg-[#FAFAF8] px-3">
          <div className="flex items-start gap-3 py-3">
            <Phone
              size={17}
              aria-hidden="true"
              className="mt-0.5 shrink-0 text-[var(--color-text-secondary)]"
            />

            <div className="min-w-0">
              <dt className="text-xs text-[var(--color-text-secondary)]">
                Mobile
              </dt>

              <dd className="mt-1 text-sm font-medium text-[var(--color-text)]">
                {profile.mobileNumber}
              </dd>
            </div>
          </div>

          <div className="flex items-start gap-3 py-3">
            <Mail
              size={17}
              aria-hidden="true"
              className="mt-0.5 shrink-0 text-[var(--color-text-secondary)]"
            />

            <div className="min-w-0">
              <dt className="text-xs text-[var(--color-text-secondary)]">
                Email
              </dt>

              <dd className="mt-1 break-all text-sm font-medium text-[var(--color-text)]">
                {profile.email || "—"}
              </dd>
            </div>
          </div>

          <div className="flex items-start gap-3 py-3">
            <Calendar
              size={17}
              aria-hidden="true"
              className="mt-0.5 shrink-0 text-[var(--color-text-secondary)]"
            />

            <div className="min-w-0">
              <dt className="text-xs text-[var(--color-text-secondary)]">
                Joined
              </dt>

              <dd className="mt-1 text-sm font-medium text-[var(--color-text)]">
                {formatDate(profile.joiningDate)}
              </dd>
            </div>
          </div>
        </dl>
      </Card.Body>
    </Card>
  );
}
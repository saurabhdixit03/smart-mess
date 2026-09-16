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

export default function ProfileCard({
  profile,
}: ProfileCardProps) {
  return (
    <Card
      className="
        w-full
        max-w-md
        transition-all
        duration-200
        hover:-translate-y-1
        hover:shadow-md
      "
    >

      <Card.Body className="space-y-6">

        <div className="flex items-start justify-between gap-4">

          <div className="flex min-w-0 items-center gap-3">

            <div
              className="
                flex
                h-10
                w-10
                shrink-0
                items-center
                justify-center
                rounded-lg
                bg-[var(--color-primary)]/10
                text-[var(--color-primary)]
              "
            >
              <UserRound size={20} />
            </div>

            <h2 className="break-words text-xl font-semibold">
              {profile.fullName}
            </h2>

          </div>

          <StatusBadge
            label={profile.status}
            variant="success"
          />

        </div>

        <div className="grid gap-5">

          <div className="flex items-center gap-3">

            <Phone
              size={18}
              className="shrink-0 text-[var(--color-primary)]"
            />

            <div className="min-w-0">

              <p className="text-xs text-[var(--color-text-secondary)]">
                Mobile
              </p>

              <p className="font-medium">
                {profile.mobileNumber}
              </p>

            </div>

          </div>

          <div className="flex items-center gap-3">

            <Mail
              size={18}
              className="shrink-0 text-[var(--color-primary)]"
            />

            <div className="min-w-0">

              <p className="text-xs text-[var(--color-text-secondary)]">
                Email
              </p>

              <p className="break-all font-medium">
                {profile.email || "-"}
              </p>

            </div>

          </div>

          <div className="flex items-center gap-3">

            <Calendar
              size={18}
              className="shrink-0 text-[var(--color-primary)]"
            />

            <div className="min-w-0">

              <p className="text-xs text-[var(--color-text-secondary)]">
                Joined
              </p>

              <p className="font-medium">
                {new Date(
                  profile.joiningDate
                ).toLocaleDateString(
                  "en-IN",
                  {
                    day: "2-digit",
                    month: "short",
                    year: "numeric",
                  }
                )}
              </p>

            </div>

          </div>

        </div>

      </Card.Body>

    </Card>
  );
}
import { Mail, Phone, Calendar } from "lucide-react";

import {
  Button,
  Card,
  StatusBadge,
} from "@/components/common/ui";

import type { CustomerProfile } from "../types";

interface ProfileCardProps {
  profile: CustomerProfile;

  onEdit: () => void;
}

export default function ProfileCard({
  profile,
  onEdit,
}: ProfileCardProps) {
  return (
    <Card>

      <Card.Body className="space-y-6">

        <div className="flex items-start justify-between">

          <div>

            <h2 className="text-2xl font-semibold">
              {profile.fullName}
            </h2>

            <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
              Customer Profile
            </p>

          </div>

          <StatusBadge
  label={profile.status}
  variant="success"
/>

        </div>

        <div
          className="
            grid
            gap-5

            sm:grid-cols-2

            lg:grid-cols-4
          "
        >

          <div className="flex items-center gap-3">

            <Phone
              size={18}
              className="text-[var(--color-primary)]"
            />

            <div>

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
              className="text-[var(--color-primary)]"
            />

            <div>

              <p className="text-xs text-[var(--color-text-secondary)]">
                Email
              </p>

              <p className="font-medium">
                {profile.email || "-"}
              </p>

            </div>

          </div>

          <div className="flex items-center gap-3">

            <Calendar
              size={18}
              className="text-[var(--color-primary)]"
            />

            <div>

              <p className="text-xs text-[var(--color-text-secondary)]">
                Joined
              </p>

              <p className="font-medium">
                {new Date(
                  profile.joiningDate
                ).toLocaleDateString()}
              </p>

            </div>

          </div>

          <div className="flex items-center justify-end">

            <Button
              onClick={onEdit}
            >
              Edit Profile
            </Button>

          </div>

        </div>

      </Card.Body>

    </Card>
  );
}
import {
  Settings,
  UtensilsCrossed,
} from "lucide-react";
import { useNavigate } from "react-router-dom";

import Card from "@/components/common/ui/Card/Card";
import Button from "@/components/common/ui/Button/Button";
import MenuSummary from "@/components/common/business/MenuSummary";
import StatusBadge from "@/components/common/ui/StatusBadge";

import type {
  MenuAvailabilityResponse,
  MenuResponse,
} from "../../types/menu.types";

type MealMenuCardProps = {
  title: string;
  menu?: MenuResponse;
  availability?: MenuAvailabilityResponse;
  onPublish?: () => void;
};

export default function MealMenuCard({
  title,
  menu,
  availability,
  onPublish,
}: MealMenuCardProps) {
  const navigate = useNavigate();

  const published = !!menu;

  const canPublish =
    availability?.canPublish ?? true;

  const reason =
    availability?.reason ?? null;

  const requiresResponseWindowSetup =
    !canPublish &&
    reason === "Response cutoff is not configured.";

  return (
    <Card className="h-full interactive-surface">
      <Card.Body className="space-y-4">
        <div className="flex items-center gap-3">
          <div className="rounded-xl bg-orange-100 p-3">
            <UtensilsCrossed
              size={20}
              className="text-orange-600"
            />
          </div>

          <div className="flex flex-1 items-start justify-between">
            <div>
              <h3 className="text-xl font-semibold">
                {title}
              </h3>

              <p className="text-sm text-[var(--color-text-secondary)]">
                {published
                  ? `Today's ${title.toLowerCase()} menu has been published.`
                  : `No ${title.toLowerCase()} menu published yet.`}
              </p>
            </div>

            {published && (
              <StatusBadge
                label="Published"
                variant="success"
              />
            )}
          </div>
        </div>

        {published ? (
          <div className="space-y-3">
            <MenuSummary
              sabjiOne={menu.sabjiOne}
              sabjiTwo={menu.sabjiTwo}
              dal={menu.dal}
              rice={menu.rice}
              sweet={menu.sweet}
            />
          </div>
        ) : (
          <div className="space-y-2">
            <Button
              className="w-full"
              onClick={onPublish}
              disabled={!canPublish}
            >
              Publish {title}
            </Button>

            {!canPublish && reason && (
              <div className="space-y-2 text-center">
                <p className="text-sm text-[var(--color-text-secondary)]">
                  {reason}
                </p>

                {requiresResponseWindowSetup && (
                  <button
                    type="button"
                    onClick={() => navigate("/owner/settings")}
                    className="inline-flex items-center gap-1.5 text-sm font-medium text-[var(--color-primary)] hover:underline"
                  >
                    <Settings size={14} />
                    Configure in Settings
                  </button>
                )}
              </div>
            )}
          </div>
        )}
      </Card.Body>
    </Card>
  );
}
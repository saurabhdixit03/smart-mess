import {
  ArrowRight,
  Settings,
  UtensilsCrossed,
} from "lucide-react";

import { useNavigate } from "react-router-dom";

import Card from "@/components/common/ui/Card/Card";
import Button from "@/components/common/ui/Button/Button";
import StatusBadge from "@/components/common/ui/StatusBadge";

import MenuSummary from "@/components/common/business/MenuSummary";

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

  const canPublish =
    availability?.canPublish ?? true;

  const reason =
    availability?.reason ?? null;

  const requiresResponseWindowSetup =
    !canPublish &&
    reason === "Response cutoff is not configured.";

  return (
    <Card className="interactive-surface flex h-full flex-col">
      <Card.Body className="flex flex-1 flex-col p-4">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-[var(--color-border)] pb-3">
          <div className="flex min-w-0 items-center gap-3">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[var(--color-primary)]/10 text-[var(--color-primary)]">
              <UtensilsCrossed
                size={20}
                aria-hidden="true"
              />
            </div>

            <h3 className="text-lg font-semibold tracking-tight text-[var(--color-text)]">
              {title}
            </h3>
          </div>

          {menu && (
            <StatusBadge
              label="Published"
              variant="success"
            />
          )}
        </div>

        {menu ? (
          <div className="mt-3 rounded-xl border border-[var(--color-border)] bg-[#FAFAF8] p-3">
            <MenuSummary
              sabjiOne={menu.sabjiOne}
              sabjiTwo={menu.sabjiTwo}
              dal={menu.dal}
              rice={menu.rice}
              sweet={menu.sweet}
            />
          </div>
        ) : (
          <div className="mt-3 flex flex-1 flex-col justify-center rounded-xl border border-dashed border-[var(--color-border)] bg-[#FAFAF8] p-4">
            <p className="text-sm font-medium text-[var(--color-text)]">
              {canPublish
                ? "Ready to publish"
                : "Publishing unavailable"}
            </p>

            <p className="mt-1 text-sm leading-5 text-[var(--color-text-secondary)]">
              {!canPublish && reason
                ? reason
                : `Add today's ${title.toLowerCase()} dishes to start collecting customer responses.`}
            </p>

            {requiresResponseWindowSetup && (
              <button
                type="button"
                onClick={() =>
                  navigate("/owner/settings")
                }
                className="mt-3 inline-flex w-fit items-center gap-1.5 rounded-md text-sm font-medium text-[var(--color-primary)] hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)] focus-visible:ring-offset-2"
              >
                <Settings
                  size={14}
                  aria-hidden="true"
                />
                Configure in Settings
              </button>
            )}
          </div>
        )}
      </Card.Body>

      {!menu && (
        <Card.Footer className="px-4 py-3">
          <Button
            type="button"
            fullWidth
            onClick={onPublish}
            disabled={!canPublish}
          >
            Publish {title}
            <ArrowRight
              size={16}
              aria-hidden="true"
            />
          </Button>
        </Card.Footer>
      )}
    </Card>
  );
}
import { useState } from "react";

import { UtensilsCrossed } from "lucide-react";
import { toast } from "sonner";

import {
  Button,
  Card,
  StatusBadge,
} from "@/components/common/ui";

import MenuSummary from "@/components/common/business/MenuSummary";

import {
  useCustomerMealResponse,
  useMealResponse,
  useMealResponseAvailability,
} from "../hooks";

import { MEAL_SESSION_LABELS } from "../constants";

import MealResponseForm from "./MealResponseForm";

import type {
  MealOption,
  MealResponseStatus,
  Menu,
} from "../types";

import { getCustomer } from "@/features/auth/utils/auth.utils";

interface MenuCardProps {
  menu: Menu;
}

interface CustomerMenuCardProps extends MenuCardProps {
  customerId: number;
}

export default function MenuCard({
  menu,
}: MenuCardProps) {
  const customer = getCustomer();

  if (!customer) {
    return (
      <Card>
        <Card.Body>
          <p
            role="alert"
            className="text-center text-sm text-[var(--color-danger)]"
          >
            Customer session not found.
          </p>
        </Card.Body>
      </Card>
    );
  }

  return (
    <CustomerMenuCard
      key={`${customer.customerId}-${menu.menuId}`}
      customerId={customer.customerId}
      menu={menu}
    />
  );
}

function CustomerMenuCard({
  customerId,
  menu,
}: CustomerMenuCardProps) {
  const [open, setOpen] = useState(false);

  const {
    loading,
    submitMealResponse,
  } = useMealResponse();

  const {
    mealResponse,
    loading: responseLoading,
    refetch,
  } = useCustomerMealResponse(
    customerId,
    menu.menuId
  );

  const {
    availability,
    loading: availabilityLoading,
    error: availabilityError,
    refetch: refetchAvailability,
  } = useMealResponseAvailability(menu.menuId);

  const canRespond =
    availability?.canRespond ?? false;

  const accepted =
    mealResponse?.responseStatus === "ACCEPTED";

  const responseUnavailableReason =
    availabilityError
      ? "Unable to check response availability. Please try again later."
      : availability?.reason ?? null;

  const responseLabel =
    mealResponse && accepted
      ? [
          mealResponse.mealOption === "FULL"
            ? "Full meal"
            : mealResponse.mealOption === "HALF"
              ? "Half meal"
              : "Accepted",
          mealResponse.extraRotiCount > 0
            ? `+${mealResponse.extraRotiCount} ${
                mealResponse.extraRotiCount === 1
                  ? "roti"
                  : "rotis"
              }`
            : null,
        ]
          .filter(Boolean)
          .join(" · ")
      : "Not Today";

  async function handleSubmit(
    responseStatus: MealResponseStatus,
    mealOption: MealOption | null,
    extraRotiCount: number
  ) {
    try {
      await submitMealResponse({
        customerId,
        menuId: menu.menuId,
        responseStatus,
        mealOption,
        extraRotiCount,
      });

      await refetch();

      toast.success(
        mealResponse
          ? "Your tiffin response has been updated."
          : "Your tiffin response has been submitted."
      );

      setOpen(false);
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : "Unable to save your response. Please try again."
      );

      try {
        await refetchAvailability();
      } catch {
        // Preserve the original submission error.
      }
    }
  }

  function closeForm() {
    if (!loading) {
      setOpen(false);
    }
  }

  return (
    <>
      <Card className="interactive-surface flex h-full flex-col">
        <Card.Body className="flex flex-1 flex-col p-4">
          <div className="flex flex-wrap items-center justify-between gap-2 border-b border-[var(--color-border)] pb-3">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[var(--color-primary)]/10 text-[var(--color-primary)]">
                <UtensilsCrossed
                  size={20}
                  aria-hidden="true"
                />
              </div>

              <div>
                <h2 className="text-lg font-semibold tracking-tight text-[var(--color-text)]">
                  {MEAL_SESSION_LABELS[menu.mealSession]}
                </h2>

              </div>
            </div>

            {!responseLoading && mealResponse && (
              <StatusBadge
                label={responseLabel}
                variant={accepted ? "success" : "warning"}
              />
            )}
          </div>

          <div className="mt-3 flex-1 rounded-xl border border-[var(--color-border)] bg-[#FAFAF8] p-3">
            <MenuSummary
              sabjiOne={menu.sabjiOne}
              sabjiTwo={menu.sabjiTwo}
              dal={menu.dal}
              rice={menu.rice}
              sweet={menu.sweet}
            />
          </div>
        </Card.Body>

        <Card.Footer className="space-y-2 px-4 py-3">
          <Button
            type="button"
            fullWidth
            disabled={
              loading ||
              responseLoading ||
              availabilityLoading ||
              !canRespond
            }
            onClick={() => setOpen(true)}
          >
            {loading
              ? "Saving..."
              : responseLoading || availabilityLoading
                ? "Checking availability..."
                : mealResponse
                  ? "Update Response"
                  : "Respond"}
          </Button>

          {!availabilityLoading &&
            !canRespond &&
            responseUnavailableReason && (
              <p className="text-center text-xs leading-5 text-[var(--color-text-secondary)]">
                {responseUnavailableReason}
              </p>
            )}
        </Card.Footer>
      </Card>

      <MealResponseForm
        open={open}
        loading={loading}
        existingResponse={mealResponse}
        onClose={closeForm}
        onSubmit={handleSubmit}
      />
    </>
  );
}
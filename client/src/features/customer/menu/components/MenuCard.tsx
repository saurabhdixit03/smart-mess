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

export default function MenuCard({
  menu,
}: MenuCardProps) {
  const customer = getCustomer();

  if (!customer) {
    return (
      <Card>
        <Card.Body>
          <p className="text-center text-red-500">
            Customer session not found.
          </p>
        </Card.Body>
      </Card>
    );
  }

  const customerId = customer.customerId;

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

      console.error(error);

      toast.error(
        "Unable to save your response. Please try again."
      );

    }
  }

  return (
    <>
      <Card className="flex h-full flex-col">

        <Card.Body className="flex flex-1 flex-col">

          <div className="flex items-start justify-between">

            <div className="flex items-center gap-4">

              <div className="rounded-xl bg-orange-100 p-3">
                <UtensilsCrossed
                  size={22}
                  className="text-orange-600"
                />
              </div>

              <div>

                <h2 className="text-xl font-semibold">
                  {MEAL_SESSION_LABELS[menu.mealSession]}
                </h2>

                <p className="text-sm text-[var(--color-text-secondary)]">
                  {menu.menuDate}
                </p>

              </div>

            </div>

            {mealResponse && (
              <StatusBadge
                label={
                  mealResponse.responseStatus === "ACCEPTED"
                    ? `Accepted • ${mealResponse.mealOption}${
                        mealResponse.extraRotiCount > 0
                          ? ` • +${mealResponse.extraRotiCount} Roti${
                              mealResponse.extraRotiCount > 1
                                ? "s"
                                : ""
                            }`
                          : ""
                      }`
                    : "Not Today"
                }
              />
            )}

          </div>

          <div className="mt-6 flex-1">

            <MenuSummary
              sabjiOne={menu.sabjiOne}
              sabjiTwo={menu.sabjiTwo}
              dal={menu.dal}
              rice={menu.rice}
              sweet={menu.sweet}
            />

          </div>

        </Card.Body>

        <Card.Footer>

          <Button
            fullWidth
            disabled={responseLoading}
            onClick={() => setOpen(true)}
          >
            {mealResponse
              ? "Update Response"
              : "Respond"}
          </Button>

        </Card.Footer>

      </Card>

      <MealResponseForm
        open={open}
        loading={loading}
        existingResponse={mealResponse}
        onClose={() => setOpen(false)}
        onSubmit={handleSubmit}
      />
    </>
  );
}
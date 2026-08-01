import { useEffect, useState } from "react";

import {
  Button,
  Modal,
} from "@/components/common/ui";

import {
  EXTRA_ROTI,
  MEAL_OPTION,
  MEAL_RESPONSE_STATUS,
} from "../constants";

import type {
  MealOption,
  MealResponse,
  MealResponseStatus,
} from "../types";

interface MealResponseFormProps {
  open: boolean;
  loading: boolean;
  existingResponse: MealResponse | null;

  onClose: () => void;

  onSubmit: (
    responseStatus: MealResponseStatus,
    mealOption: MealOption | null,
    extraRotiCount: number
  ) => Promise<void>;
}

export default function MealResponseForm({
  open,
  loading,
  existingResponse,
  onClose,
  onSubmit,
}: MealResponseFormProps) {
  const [responseStatus, setResponseStatus] =
    useState<MealResponseStatus>(
      MEAL_RESPONSE_STATUS.ACCEPTED
    );

  const [mealOption, setMealOption] =
    useState<MealOption>(
      MEAL_OPTION.FULL
    );

  const [extraRotiCount, setExtraRotiCount] =
    useState<number>(
      EXTRA_ROTI.DEFAULT
    );

  useEffect(() => {
    if (!open) return;

    setResponseStatus(
      existingResponse?.responseStatus ??
        MEAL_RESPONSE_STATUS.ACCEPTED
    );

    setMealOption(
      existingResponse?.mealOption ??
        MEAL_OPTION.FULL
    );

    setExtraRotiCount(
      existingResponse?.extraRotiCount ??
        EXTRA_ROTI.DEFAULT
    );
  }, [open, existingResponse]);

  async function handleSubmit() {
    await onSubmit(
      responseStatus,
      responseStatus ===
        MEAL_RESPONSE_STATUS.ACCEPTED
        ? mealOption
        : null,
      responseStatus ===
        MEAL_RESPONSE_STATUS.ACCEPTED
        ? extraRotiCount
        : 0
    );

    onClose();
  }

  function increaseRoti() {
    setExtraRotiCount((value) =>
      Math.min(value + 1, EXTRA_ROTI.MAX)
    );
  }

  function decreaseRoti() {
    setExtraRotiCount((value) =>
      Math.max(value - 1, EXTRA_ROTI.MIN)
    );
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={
        existingResponse
          ? "Update Tiffin Response"
          : "Respond to Today's Menu"
      }
      footer={
        <>
          <Button
            variant="outline"
            onClick={onClose}
          >
            Cancel
          </Button>

          <Button
            disabled={loading}
            onClick={handleSubmit}
          >
            {existingResponse
              ? "Update Response"
              : "Save Response"}
          </Button>
        </>
      }
    >
      <div className="space-y-7">

        <p className="text-sm leading-6 text-[var(--color-text-secondary)]">
          Your response helps us prepare the right
          quantity of food.
        </p>

        <div>

          <p className="mb-3 text-sm font-semibold">
            Will you be taking today's tiffin?
          </p>

          <div className="flex gap-3">

            <Button
              fullWidth
              variant={
                responseStatus ===
                MEAL_RESPONSE_STATUS.ACCEPTED
                  ? "primary"
                  : "outline"
              }
              onClick={() =>
                setResponseStatus(
                  MEAL_RESPONSE_STATUS.ACCEPTED
                )
              }
            >
              I'll Eat
            </Button>

            <Button
              fullWidth
              variant={
                responseStatus ===
                MEAL_RESPONSE_STATUS.DECLINED
                  ? "primary"
                  : "outline"
              }
              onClick={() =>
                setResponseStatus(
                  MEAL_RESPONSE_STATUS.DECLINED
                )
              }
            >
              Not Today
            </Button>

          </div>

        </div>

        {responseStatus ===
          MEAL_RESPONSE_STATUS.ACCEPTED && (

          <div className="space-y-6">

            <div>

              <p className="mb-3 text-sm font-semibold">
                Select Tiffin Type
              </p>

              <div className="flex gap-3">

                <Button
                  fullWidth
                  variant={
                    mealOption ===
                    MEAL_OPTION.FULL
                      ? "primary"
                      : "outline"
                  }
                  onClick={() =>
                    setMealOption(
                      MEAL_OPTION.FULL
                    )
                  }
                >
                  Full
                </Button>

                <Button
                  fullWidth
                  variant={
                    mealOption ===
                    MEAL_OPTION.HALF
                      ? "primary"
                      : "outline"
                  }
                  onClick={() =>
                    setMealOption(
                      MEAL_OPTION.HALF
                    )
                  }
                >
                  Half
                </Button>

              </div>

            </div>

            <div>

              <div className="mb-3 flex items-center justify-between">

                <p className="text-sm font-semibold">
                  Extra Rotis
                </p>

                <span className="text-xs text-[var(--color-text-secondary)]">
                  Optional
                </span>

              </div>

              <div className="flex items-center gap-4">

                <Button
                  size="sm"
                  variant="outline"
                  disabled={
                    extraRotiCount ===
                    EXTRA_ROTI.MIN
                  }
                  onClick={decreaseRoti}
                >
                  −
                </Button>

                <span className="min-w-8 text-center text-lg font-semibold">
                  {extraRotiCount}
                </span>

                <Button
                  size="sm"
                  variant="outline"
                  disabled={
                    extraRotiCount ===
                    EXTRA_ROTI.MAX
                  }
                  onClick={increaseRoti}
                >
                  +
                </Button>

              </div>

            </div>

          </div>

        )}

      </div>
    </Modal>
  );
}
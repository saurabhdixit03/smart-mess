import { useState } from "react";

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

export default function MealResponseForm(
  props: MealResponseFormProps
) {
  if (!props.open) {
    return null;
  }

  const response = props.existingResponse;

  const formKey = JSON.stringify([
    response?.responseStatus ?? null,
    response?.mealOption ?? null,
    response?.extraRotiCount ?? null,
  ]);

  return (
    <OpenMealResponseForm
      key={formKey}
      {...props}
    />
  );
}

function OpenMealResponseForm({
  open,
  loading,
  existingResponse,
  onClose,
  onSubmit,
}: MealResponseFormProps) {
  const [responseStatus, setResponseStatus] =
    useState<MealResponseStatus>(
      existingResponse?.responseStatus ??
        MEAL_RESPONSE_STATUS.ACCEPTED
    );

  const [mealOption, setMealOption] =
    useState<MealOption>(
      existingResponse?.mealOption ??
        MEAL_OPTION.FULL
    );

  const [extraRotiCount, setExtraRotiCount] =
    useState<number>(
      existingResponse?.extraRotiCount ??
        EXTRA_ROTI.DEFAULT
    );

  const accepted =
    responseStatus === MEAL_RESPONSE_STATUS.ACCEPTED;

  async function handleSubmit() {
    if (loading) {
      return;
    }

    await onSubmit(
      responseStatus,
      accepted ? mealOption : null,
      accepted ? extraRotiCount : 0
    );
  }

  function handleClose() {
    if (!loading) {
      onClose();
    }
  }

  return (
    <Modal
      open={open}
      onClose={handleClose}
      title={
        existingResponse
          ? "Update Tiffin Response"
          : "Respond to Today's Menu"
      }
      footer={
        <>
          <Button
            type="button"
            variant="secondary"
            disabled={loading}
            onClick={handleClose}
          >
            Cancel
          </Button>

          <Button
            type="button"
            disabled={loading}
            onClick={() => void handleSubmit()}
          >
            {loading
              ? "Saving..."
              : existingResponse
                ? "Update Response"
                : "Save Response"}
          </Button>
        </>
      }
    >
      <div className="space-y-5">
        <p className="text-sm leading-5 text-[var(--color-text-secondary)]">
          Your response helps your mess prepare
          the right quantity of food.
        </p>

        <fieldset disabled={loading}>
          <legend className="mb-2 text-sm font-semibold text-[var(--color-text)]">
            Will you be taking today's tiffin?
          </legend>

          <div className="grid grid-cols-2 gap-2">
            <Button
              type="button"
              fullWidth
              disabled={loading}
              aria-pressed={accepted}
              variant={accepted ? "primary" : "outline"}
              onClick={() =>
                setResponseStatus(
                  MEAL_RESPONSE_STATUS.ACCEPTED
                )
              }
            >
              I'll Eat
            </Button>

            <Button
              type="button"
              fullWidth
              disabled={loading}
              aria-pressed={!accepted}
              variant={!accepted ? "primary" : "outline"}
              onClick={() =>
                setResponseStatus(
                  MEAL_RESPONSE_STATUS.DECLINED
                )
              }
            >
              Not Today
            </Button>
          </div>
        </fieldset>

        {accepted ? (
          <div className="space-y-4">
            <fieldset disabled={loading}>
              <legend className="mb-2 text-sm font-semibold text-[var(--color-text)]">
                Tiffin type
              </legend>

              <div className="grid grid-cols-2 gap-2">
                <Button
                  type="button"
                  fullWidth
                  disabled={loading}
                  aria-pressed={
                    mealOption === MEAL_OPTION.FULL
                  }
                  variant={
                    mealOption === MEAL_OPTION.FULL
                      ? "primary"
                      : "outline"
                  }
                  onClick={() =>
                    setMealOption(MEAL_OPTION.FULL)
                  }
                >
                  Full
                </Button>

                <Button
                  type="button"
                  fullWidth
                  disabled={loading}
                  aria-pressed={
                    mealOption === MEAL_OPTION.HALF
                  }
                  variant={
                    mealOption === MEAL_OPTION.HALF
                      ? "primary"
                      : "outline"
                  }
                  onClick={() =>
                    setMealOption(MEAL_OPTION.HALF)
                  }
                >
                  Half
                </Button>
              </div>
            </fieldset>

            <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] p-3">
              <div>
                <p className="text-sm font-semibold text-[var(--color-text)]">
                  Extra rotis
                </p>

                <p className="mt-0.5 text-xs text-[var(--color-text-secondary)]">
                  Optional
                </p>
              </div>

              <div className="flex items-center gap-3">
                <Button
                  type="button"
                  size="sm"
                  variant="outline"
                  aria-label="Remove one extra roti"
                  disabled={
                    loading ||
                    extraRotiCount <= EXTRA_ROTI.MIN
                  }
                  onClick={() =>
                    setExtraRotiCount((value) =>
                      Math.max(
                        value - 1,
                        EXTRA_ROTI.MIN
                      )
                    )
                  }
                >
                  −
                </Button>

                <span
                  aria-live="polite"
                  aria-atomic="true"
                  className="min-w-6 text-center text-lg font-semibold tabular-nums text-[var(--color-text)]"
                >
                  {extraRotiCount}
                </span>

                <Button
                  type="button"
                  size="sm"
                  variant="outline"
                  aria-label="Add one extra roti"
                  disabled={
                    loading ||
                    extraRotiCount >= EXTRA_ROTI.MAX
                  }
                  onClick={() =>
                    setExtraRotiCount((value) =>
                      Math.min(
                        value + 1,
                        EXTRA_ROTI.MAX
                      )
                    )
                  }
                >
                  +
                </Button>
              </div>
            </div>
          </div>
        ) : (
          <p className="rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] p-3 text-sm text-[var(--color-text-secondary)]">
            Your mess will know you are not taking
            this meal.
          </p>
        )}
      </div>
    </Modal>
  );
}
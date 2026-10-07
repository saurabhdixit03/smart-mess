import {
  useRef,
  useState,
} from "react";

import { toast } from "sonner";

import Button from "@/components/common/ui/Button/Button";
import Modal from "@/components/common/ui/Modal/Modal";

import { useRecordMeal } from "../hooks";

import type {
  MealCollectionSelection,
  MealOption,
} from "../types";

type RecordMealDialogProps = {
  open: boolean;
  customer: MealCollectionSelection | null;
  onClose: () => void;
  onSuccess: () => void;
};

export default function RecordMealDialog(
  props: RecordMealDialogProps
) {
  if (!props.open || !props.customer) {
    return null;
  }

  const formKey = JSON.stringify([
    props.customer.customerId,
    props.customer.menuId,
    props.customer.mealResponseId,
    props.customer.mealOption,
    props.customer.extraRotiCount,
  ]);

  return (
    <RecordMealDialogContent
      key={formKey}
      {...props}
    />
  );
}

function RecordMealDialogContent({
  open,
  customer,
  onClose,
  onSuccess,
}: RecordMealDialogProps) {
  const [mealOption, setMealOption] =
    useState<MealOption>(
      customer?.mealOption ?? "FULL"
    );

  const [extraRotiCount, setExtraRotiCount] =
    useState(customer?.extraRotiCount ?? 0);

  const [submitting, setSubmitting] = useState(false);

  const [submissionError, setSubmissionError] =
    useState<string | null>(null);

  const submittingRef = useRef(false);

  const {
    recordMeal,
    loading,
    error,
  } = useRecordMeal();

  const busy = loading || submitting;

  function handleClose() {
    if (!loading && !submittingRef.current) {
      onClose();
    }
  }

  async function handleRecord() {
    if (!customer || loading || submittingRef.current) {
      return;
    }

    if (
      !Number.isInteger(extraRotiCount) ||
      extraRotiCount < 0 ||
      extraRotiCount > 5
    ) {
      setSubmissionError(
        "Extra roti count must be between 0 and 5."
      );
      return;
    }

    submittingRef.current = true;
    setSubmitting(true);
    setSubmissionError(null);

    let saved = false;

    try {
      const response = await recordMeal({
        customerId: customer.customerId,
        menuId: customer.menuId,
        mealResponseId: customer.mealResponseId,
        mealOption,
        extraRotiCount,
      });

      saved = Boolean(response);
    } catch (err: unknown) {
      setSubmissionError(
        err instanceof Error
          ? err.message
          : "Unable to record the meal."
      );
    } finally {
      submittingRef.current = false;
      setSubmitting(false);
    }

    if (saved) {
      toast.success("Meal recorded successfully.");
      onClose();
      onSuccess();
    }
  }

  return (
    <Modal
      open={open}
      size="sm"
      title="Record Meal"
      onClose={handleClose}
      footer={
        <>
          <Button
            type="button"
            variant="secondary"
            onClick={handleClose}
            disabled={busy}
          >
            Cancel
          </Button>

          <Button
            type="button"
            onClick={() => void handleRecord()}
            disabled={busy || !customer}
          >
            {busy ? "Saving..." : "Save"}
          </Button>
        </>
      }
    >
      {customer && (
        <div className="space-y-5">
          <div>
            <p className="text-sm text-[var(--color-text-secondary)]">
              Customer
            </p>

            <p className="mt-1 font-medium">
              {customer.customerName}
            </p>
          </div>

          <fieldset disabled={busy}>
            <legend className="mb-2 text-sm font-medium">
              Meal Type
            </legend>

            <div className="flex gap-6">
              <label className="flex cursor-pointer items-center gap-2">
                <input
                  type="radio"
                  name="collection-meal-option"
                  value="FULL"
                  checked={mealOption === "FULL"}
                  onChange={() => setMealOption("FULL")}
                />
                Full Meal
              </label>

              <label className="flex cursor-pointer items-center gap-2">
                <input
                  type="radio"
                  name="collection-meal-option"
                  value="HALF"
                  checked={mealOption === "HALF"}
                  onChange={() => setMealOption("HALF")}
                />
                Half Meal
              </label>
            </div>
          </fieldset>

          <div>
            <p className="mb-2 text-sm font-medium">
              Extra Rotis
            </p>

            <div className="flex items-center gap-3">
              <Button
                type="button"
                variant="secondary"
                aria-label="Remove one extra roti"
                disabled={busy || extraRotiCount <= 0}
                onClick={() => {
                  setExtraRotiCount((value) =>
                    Math.max(0, value - 1)
                  );
                  setSubmissionError(null);
                }}
              >
                −
              </Button>

              <span
                aria-live="polite"
                className="w-8 text-center font-semibold"
              >
                {extraRotiCount}
              </span>

              <Button
                type="button"
                variant="secondary"
                aria-label="Add one extra roti"
                disabled={busy || extraRotiCount >= 5}
                onClick={() => {
                  setExtraRotiCount((value) =>
                    Math.min(5, value + 1)
                  );
                  setSubmissionError(null);
                }}
              >
                +
              </Button>
            </div>
          </div>

          {(submissionError || error) && (
            <p
              role="alert"
              className="text-sm text-red-500"
            >
              {submissionError || error}
            </p>
          )}
        </div>
      )}
    </Modal>
  );
}
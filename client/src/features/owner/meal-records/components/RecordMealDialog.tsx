import { useEffect, useState } from "react";

import { toast } from "sonner";

import Button from "@/components/common/ui/Button/Button";
import Modal from "@/components/common/ui/Modal/Modal";

import { useRecordMeal } from "../hooks";

import type {
  CollectionQueueItem,
} from "../types";

type RecordMealDialogProps = {

  open: boolean;

  customer: CollectionQueueItem | null;

  onClose: () => void;

  onSuccess: () => void;

};

export default function RecordMealDialog({

  open,

  customer,

  onClose,

  onSuccess,

}: RecordMealDialogProps) {

  const [mealOption, setMealOption] = useState<
    "FULL" | "HALF"
  >("HALF");

  const [extraRotiCount, setExtraRotiCount] =
    useState(0);

  const {
    recordMeal,
    loading,
    error,
  } = useRecordMeal();

  useEffect(() => {

    if (customer) {

      setMealOption(customer.mealOption);

      setExtraRotiCount(
        customer.extraRotiCount
      );

    }

  }, [customer]);

  async function handleRecord() {

    if (!customer) {
      return;
    }

    const response = await recordMeal({

      customerId: customer.customerId,

      menuId: customer.menuId,

      mealResponseId: customer.mealResponseId,

      mealOption,

      extraRotiCount,

    });

    if (!response) {
      return;
    }

    toast.success("Meal recorded successfully.");

    onClose();

    onSuccess();

  }

  return (

    <Modal
      open={open}
      size="sm"
      title="Record Meal"
      onClose={onClose}
      footer={
        <>
          <Button
            variant="secondary"
            onClick={onClose}
            disabled={loading}
          >
            Cancel
          </Button>

          <Button
            onClick={handleRecord}
            disabled={loading}
          >
            {loading
              ? "Saving..."
              : "Save"}
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

          <div>

            <p className="mb-2 text-sm font-medium">
              Meal Type
            </p>

            <div className="flex gap-6">

              <label className="flex cursor-pointer items-center gap-2">

                <input
                  type="radio"
                  checked={mealOption === "FULL"}
                  onChange={() =>
                    setMealOption("FULL")
                  }
                />

                Full Meal

              </label>

              <label className="flex cursor-pointer items-center gap-2">

                <input
                  type="radio"
                  checked={mealOption === "HALF"}
                  onChange={() =>
                    setMealOption("HALF")
                  }
                />

                Half Meal

              </label>

            </div>

          </div>

          <div>

            <p className="mb-2 text-sm font-medium">
              Extra Rotis
            </p>

            <div className="flex items-center gap-3">

              <Button
                variant="secondary"
                onClick={() =>
                  setExtraRotiCount((value) =>
                    Math.max(0, value - 1)
                  )
                }
              >
                -
              </Button>

              <span className="w-8 text-center font-semibold">
                {extraRotiCount}
              </span>

              <Button
                variant="secondary"
                onClick={() =>
                  setExtraRotiCount(
                    (value) => value + 1
                  )
                }
              >
                +
              </Button>

            </div>

          </div>

          {error && (

            <p className="text-sm text-red-500">
              {error}
            </p>

          )}

        </div>

      )}

    </Modal>

  );

}
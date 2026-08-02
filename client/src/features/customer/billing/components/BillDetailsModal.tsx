import { useEffect } from "react";

import {
  Modal,
  StatusBadge,
} from "@/components/common/ui";

import { useBillDetails } from "../hooks";

import MealBreakdownList from "./MealBreakdownList";

interface BillDetailsModalProps {
  billId: number | null;
  open: boolean;
  onClose: () => void;
}

const MONTHS = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
];

export default function BillDetailsModal({
  billId,
  open,
  onClose,
}: BillDetailsModalProps) {
  const {
    billDetail,
    loading,
    error,
    fetchBillDetails,
  } = useBillDetails();

  useEffect(() => {
    if (!open || billId === null) {
      return;
    }

    fetchBillDetails(billId);
  }, [
    open,
    billId,
    fetchBillDetails,
  ]);

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Bill Details"
      size="lg"
    >
      <div className="space-y-6">

        {loading && (
          <div className="flex h-[380px] items-center justify-center">
            <p className="text-[var(--color-text-secondary)]">
              Loading bill details...
            </p>
          </div>
        )}

        {!loading && error && (
          <div className="flex h-[380px] items-center justify-center">
            <p className="text-red-500">
              {error}
            </p>
          </div>
        )}

        {!loading && !error && billDetail && (
          <div className="space-y-6">

            {/* Bill Summary */}

            <div className="rounded-2xl border border-[var(--color-border)] p-4">

              <div className="flex items-start justify-between">

                <div>
                  <h2 className="text-xl font-bold">
                    {
                      MONTHS[
                        billDetail.billingMonth - 1
                      ]
                    }{" "}
                    {billDetail.billingYear}
                  </h2>

                  <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
                    {billDetail.mealRecordCount} Meals
                  </p>
                </div>

                <StatusBadge
                  label={billDetail.billStatus}
                  variant={
                    billDetail.billStatus === "PAID"
                      ? "success"
                      : "warning"
                  }
                />

              </div>

              <div className="mt-6 border-t border-[var(--color-border)] pt-5">

                <div className="flex items-center justify-between">

                  <span className="text-[var(--color-text-secondary)]">
                    Total Amount
                  </span>

                  <span className="text-3xl font-bold">
                    ₹{billDetail.totalAmount}
                  </span>

                </div>

              </div>

            </div>

            {/* Meal Breakdown */}

{/* Meal Breakdown */}

<div>

  <h3 className="mb-4 text-lg font-semibold">
    Meal Breakdown
  </h3>

  <MealBreakdownList
    mealRecords={billDetail.mealRecords}
  />

</div>

          </div>
        )}

      </div>
    </Modal>
  );
}
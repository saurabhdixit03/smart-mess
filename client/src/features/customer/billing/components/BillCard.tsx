import { Receipt } from "lucide-react";

import {
  Button,
  Card,
  StatusBadge,
} from "@/components/common/ui";

import type { Bill } from "../types";

interface BillCardProps {
  bill: Bill;

  onView: (
    billId: number
  ) => void;

  onPay: (
    billId: number
  ) => void;
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

export default function BillCard({
  bill,
  onView,
  onPay,
}: BillCardProps) {
  return (
    <Card className="flex h-full flex-col">

      <Card.Body className="flex flex-1 flex-col">

        <div className="flex items-start justify-between">

          <div className="flex items-center gap-4">

            <div className="rounded-xl bg-green-100 p-3">

              <Receipt
                size={22}
                className="text-green-600"
              />

            </div>

            <div>

              <h2 className="text-lg font-semibold">
                {MONTHS[bill.billingMonth - 1]}{" "}
                {bill.billingYear}
              </h2>

              <p className="text-sm text-[var(--color-text-secondary)]">
                {bill.mealRecordCount} Meals
              </p>

            </div>

          </div>

          <StatusBadge
            label={bill.billStatus}
            variant={
              bill.billStatus === "PAID"
                ? "success"
                : "warning"
            }
          />

        </div>

        <div className="mt-6 space-y-2">

          <div className="flex justify-between">

            <span className="text-sm text-[var(--color-text-secondary)]">
              Total Amount
            </span>

            <span className="font-semibold">
              ₹{bill.totalAmount}
            </span>

          </div>

          <div className="flex justify-between">

            <span className="text-sm text-[var(--color-text-secondary)]">
              Generated
            </span>

            <span className="text-sm">
              {new Date(
                bill.generatedAt
              ).toLocaleDateString()}
            </span>

          </div>

        </div>

      </Card.Body>

      <Card.Footer className="space-y-3">

        <Button
          fullWidth
          variant="secondary"
          onClick={() =>
            onView(bill.billId)
          }
        >
          View Bill
        </Button>

        {bill.billStatus === "UNPAID" && (
          <Button
            fullWidth
            onClick={() =>
              onPay(bill.billId)
            }
          >
            Pay Now
          </Button>
        )}

        {bill.billStatus === "PAYMENT_PENDING" && (
          <Button
            fullWidth
            disabled
          >
            Waiting for Approval
          </Button>
        )}

      </Card.Footer>

    </Card>
  );
}
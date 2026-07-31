import type { Column } from "@/components/common/ui/DataTable/DataTable";

import { Button } from "@/components/common/ui";

import type { PendingPaymentResponse } from "../types";

function formatBillingMonth(
  month: number,
  year: number
) {
  return new Date(
    year,
    month - 1
  ).toLocaleDateString("en-IN", {
    month: "short",
    year: "numeric",
  });
}

type PendingPaymentColumnsProps = {
  onApprove: (
    billId: number
  ) => void;
};

export function pendingPaymentColumns({
  onApprove,
}: PendingPaymentColumnsProps): Column<PendingPaymentResponse>[] {
  return [
    {
      key: "customerName",
      header: "Customer",
    },

    {
      key: "billingMonth",
      header: "Billing Period",
      render: (row) =>
        formatBillingMonth(
          row.billingMonth,
          row.billingYear
        ),
    },

    {
      key: "totalAmount",
      header: "Amount",
      className: "text-right font-semibold",
      headerClassName: "text-right",
      render: (row) =>
        `₹${row.totalAmount}`,
    },

    {
      key: "actions",
      header: "",
      className: "text-right",
      render: (row) => (
        <Button
          size="sm"
          onClick={() =>
            onApprove(row.billId)
          }
        >
          Approve
        </Button>
      ),
    },
  ];
}
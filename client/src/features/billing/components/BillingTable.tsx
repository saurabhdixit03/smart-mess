import { Button, DataTable, StatusBadge } from "@/components/common/ui";

import type { Column } from "@/components/common/ui/DataTable/DataTable";
import type { BillResponse } from "../types";

type BillingTableProps = {
  bills: BillResponse[];
  onViewBill: (billId: number) => void;
};



function formatDate(date: string) {
  return new Date(date).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function getBillStatusVariant(
  status: BillResponse["billStatus"]
): "success" | "warning" | "info" {
  switch (status) {
    case "PAID":
      return "success";

    case "UNPAID":
    case "PAYMENT_PENDING":
      return "warning";

    default:
      return "info";
  }
}

export default function BillingTable({
  bills,
  onViewBill,
}: BillingTableProps) {
  const columns: readonly Column<BillResponse>[] = [
    {
      key: "customerName",
      header: "Customer",
    },
    
    {
      key: "mealRecordCount",
      header: "Meals",
    },
    {
      key: "totalAmount",
      header: "Amount",
      render: (bill) => `₹${bill.totalAmount}`,
    },
    {
      key: "billStatus",
      header: "Status",
      render: (bill) => (
        <StatusBadge
          label={bill.billStatus}
          variant={getBillStatusVariant(
            bill.billStatus
          )}
        />
      ),
    },
    {
      key: "generatedAt",
      header: "Generated",
      render: (bill) =>
        formatDate(bill.generatedAt),
    },
    {
      key: "actions",
      header: "Actions",
      render: (bill) => (
        <Button
          variant="outline"
          size="sm"
          onClick={() =>
            onViewBill(bill.billId)
          }
        >
          View Details
        </Button>
      ),
    },
  ];

  return (
    <DataTable
      columns={columns}
      data={bills}
      rowKey={(bill) => bill.billId}
    />
  );
}
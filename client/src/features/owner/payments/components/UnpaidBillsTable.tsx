import Button from "@/components/common/ui/Button/Button";
import DataTable from "@/components/common/ui/DataTable/DataTable";
import type { Column } from "@/components/common/ui/DataTable/DataTable";

import type { BillResponse } from "../types";

type UnpaidBillsTableProps = {
  bills: BillResponse[];
  onCollectCash: (bill: BillResponse) => void;
};

export default function UnpaidBillsTable({
  bills,
  onCollectCash,
}: UnpaidBillsTableProps) {
  const columns: Column<BillResponse>[] = [
    {
      key: "customerName",
      header: "Customer",
    },
    {
      key: "billingPeriod",
      header: "Billing Period",
      render: (bill) =>
        `${bill.billingMonth}/${bill.billingYear}`,
    },
    {
      key: "totalAmount",
      header: "Amount",
      className: "text-right font-medium",
      headerClassName: "text-right",
      render: (bill) =>
        `₹${Number(bill.totalAmount).toLocaleString("en-IN")}`,
    },
    {
      key: "billStatus",
      header: "Status",
      className: "text-center",
      headerClassName: "text-center",
    },
    {
      key: "actions",
      header: "Actions",
      className: "text-center",
      headerClassName: "text-center",
      render: (bill) => (
        <Button
          size="sm"
          onClick={() => onCollectCash(bill)}
        >
          Collect Cash
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-4">

      <div>
        <h2 className="text-lg font-semibold">
          Unpaid Bills
        </h2>

        <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
          Bills awaiting payment collection.
        </p>
      </div>

      <DataTable
        columns={columns}
        data={bills}
        rowKey={(bill) => bill.billId}
      />

    </div>
  );
}
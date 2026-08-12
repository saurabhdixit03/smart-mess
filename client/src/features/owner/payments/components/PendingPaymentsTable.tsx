import Button from "@/components/common/ui/Button/Button";
import DataTable from "@/components/common/ui/DataTable/DataTable";

import type { Column } from "@/components/common/ui/DataTable/DataTable";

import type {
  PendingPaymentResponse,
} from "../types";

type PendingPaymentsTableProps = {
  pendingPayments: PendingPaymentResponse[];
  onApprovePayment: (
    payment: PendingPaymentResponse
  ) => void;
};

export default function PendingPaymentsTable({
  pendingPayments,
  onApprovePayment,
}: PendingPaymentsTableProps) {

  const columns: Column<PendingPaymentResponse>[] = [
    {
      key: "customerName",
      header: "Customer",
    },
    {
      key: "billingPeriod",
      header: "Billing Period",
      render: (payment) =>
        `${payment.billingMonth}/${payment.billingYear}`,
    },
    {
      key: "totalAmount",
      header: "Amount",
      className: "text-right font-medium",
      headerClassName: "text-right",
      render: (payment) =>
        `₹${Number(payment.totalAmount).toLocaleString("en-IN")}`,
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
      render: (payment) => (
        <Button
          size="sm"
          onClick={() =>
            onApprovePayment(payment)
          }
        >
          Approve
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-4">

      <div>

        <h2 className="text-lg font-semibold">
          Pending UPI Requests
        </h2>

        <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
          Customers waiting for payment approval.
        </p>

      </div>

      <DataTable
        columns={columns}
        data={pendingPayments}
        rowKey={(payment) => payment.billId}
      />

    </div>
  );
}
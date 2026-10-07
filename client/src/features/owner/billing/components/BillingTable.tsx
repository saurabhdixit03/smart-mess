import Button from "@/components/common/ui/Button/Button";
import DataTable from "@/components/common/ui/DataTable/DataTable";

import type { Column } from "@/components/common/ui/DataTable/DataTable";

import { StatusBadge } from "@/components/common/ui";

import type { Bill } from "../types";

interface BillingTableProps {
  bills: Bill[];
  onViewBill: (billId: number) => void;
}

const currencyFormat = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
});

const MONTHS = [
  "Jan",
  "Feb",
  "Mar",
  "Apr",
  "May",
  "Jun",
  "Jul",
  "Aug",
  "Sep",
  "Oct",
  "Nov",
  "Dec",
];

function formatDate(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return date.toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

export default function BillingTable({
  bills,
  onViewBill,
}: BillingTableProps) {
  const columns: Column<Bill>[] = [
    {
      key: "billId",
      header: "Bill",
      render: (bill) => (
        <span className="font-medium">
          #{bill.billId}
        </span>
      ),
    },
    {
      key: "customerName",
      header: "Customer",
    },
    {
      key: "billingPeriod",
      header: "Billing Period",
      render: (bill) =>
        `${MONTHS[bill.billingMonth - 1]} ${bill.billingYear}`,
    },
    {
      key: "mealRecordCount",
      header: "Meals",
      className: "text-right",
      headerClassName: "text-right",
    },
    {
      key: "totalAmount",
      header: "Amount",
      className: "text-right font-medium",
      headerClassName: "text-right",
      render: (bill) =>
        currencyFormat.format(bill.totalAmount),
    },
    {
      key: "billStatus",
      header: "Status",
      className: "text-center",
      headerClassName: "text-center",
      render: (bill) => (
        <StatusBadge
          label={
            bill.billStatus === "PAID"
              ? "Paid"
              : "Unpaid"
          }
          variant={
            bill.billStatus === "PAID"
              ? "success"
              : "warning"
          }
        />
      ),
    },
    {
      key: "generatedAt",
      header: "Issued",
      render: (bill) => formatDate(bill.generatedAt),
    },
    {
      key: "actions",
      header: "Document",
      className: "text-center",
      headerClassName: "text-center",
      render: (bill) => (
        <Button
          size="sm"
          variant="secondary"
          onClick={() => onViewBill(bill.billId)}
        >
          View Bill
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
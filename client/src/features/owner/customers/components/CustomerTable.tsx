import Card from "@/components/common/ui/Card/Card";
import Button from "@/components/common/ui/Button/Button";

import DataTable, {
  type Column,
} from "@/components/common/ui/DataTable";

import StatusBadge from "@/components/common/ui/StatusBadge";
import Pagination from "@/components/common/ui/Pagination";

import {
  MessageSquareMore,
  UserCheck,
  UserPen,
  UserRoundPlus,
  UserX,
} from "lucide-react";

import type { CustomerResponse } from "../types/customer.types";

type CustomerTableProps = {
  customers: CustomerResponse[];
  loading: boolean;
  error: string | null;
  onEdit: (customer: CustomerResponse) => void;
  onApprove: (customer: CustomerResponse) => void;
  onReactivate: (customer: CustomerResponse) => void;
  onDeactivate: (customer: CustomerResponse) => void;
  actionsDisabled?: boolean;
  currentPage: number;
  totalPages: number;
  onPrevious: () => void;
  onNext: () => void;
};

function formatDate(value: string): string {
  const date = new Date(`${value.slice(0, 10)}T00:00:00`);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return date.toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

export default function CustomerTable({
  customers,
  loading,
  error,
  onEdit,
  onApprove,
  onReactivate,
  onDeactivate,
  actionsDisabled = false,
  currentPage,
  totalPages,
  onPrevious,
  onNext,
}: CustomerTableProps) {
  if (loading) {
    return (
      <Card>
        <Card.Body className="py-12 text-center text-[var(--color-text-secondary)]">
          <p role="status">Loading customers...</p>
        </Card.Body>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <Card.Body className="py-12 text-center text-[var(--color-danger)]">
          <p role="alert">{error}</p>
        </Card.Body>
      </Card>
    );
  }

  const columns: Column<CustomerResponse>[] = [
    {
      key: "fullName",
      header: "Customer",
      render: (customer) => (
        <div className="space-y-1">
          <p className="font-medium">
            {customer.fullName}
          </p>

          {customer.remarks?.trim() && (
            <div
              title={customer.remarks}
              className="flex items-center gap-1 text-xs text-amber-600"
            >
              <MessageSquareMore
                size={13}
                aria-hidden="true"
                className="shrink-0"
              />

              <span className="max-w-[200px] truncate">
                {customer.remarks}
              </span>
            </div>
          )}
        </div>
      ),
    },
    {
      key: "mobileNumber",
      header: "Mobile",
      className: "whitespace-nowrap",
    },
    {
      key: "email",
      header: "Email",
      render: (customer) => customer.email || "—",
    },
    {
      key: "joiningDate",
      header: "Joined",
      className: "whitespace-nowrap",
      render: (customer) =>
        formatDate(customer.joiningDate),
    },
    {
      key: "status",
      header: "Status",
      headerClassName: "text-center",
      className: "text-center",
      render: (customer) => (
        <StatusBadge
          label={
            customer.status === "PENDING"
              ? "Pending"
              : customer.status === "ACTIVE"
                ? "Active"
                : "Inactive"
          }
          variant={
            customer.status === "PENDING"
              ? "warning"
              : customer.status === "ACTIVE"
                ? "success"
                : "danger"
          }
        />
      ),
    },
    {
      key: "actions",
      header: "Actions",
      headerClassName: "text-center",
      className: "text-center",
      render: (customer) => (
        <div className="flex items-center justify-center gap-2">
          <Button
            type="button"
            size="sm"
            variant="secondary"
            className="p-2"
            title="Manage customer"
            aria-label={`Manage ${customer.fullName}`}
            disabled={actionsDisabled}
            onClick={() => onEdit(customer)}
          >
            <UserPen size={16} aria-hidden="true" />
          </Button>

          {customer.status === "PENDING" && (
            <Button
              type="button"
              size="sm"
              aria-label={`Approve ${customer.fullName}`}
              disabled={actionsDisabled}
              onClick={() => onApprove(customer)}
            >
              <UserCheck size={16} aria-hidden="true" />
              Approve
            </Button>
          )}

          {customer.status === "INACTIVE" && (
            <Button
              type="button"
              size="sm"
              variant="outline"
              className="p-2"
              title="Reactivate customer"
              aria-label={`Reactivate ${customer.fullName}`}
              disabled={actionsDisabled}
              onClick={() => onReactivate(customer)}
            >
              <UserRoundPlus size={16} aria-hidden="true" />
            </Button>
          )}

          {customer.status === "ACTIVE" && (
            <Button
              type="button"
              size="sm"
              variant="danger"
              className="p-2"
              title="Deactivate customer"
              aria-label={`Deactivate ${customer.fullName}`}
              disabled={actionsDisabled}
              onClick={() => onDeactivate(customer)}
            >
              <UserX size={16} aria-hidden="true" />
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <section aria-label="Customers" className="space-y-3">
      <DataTable
        columns={columns}
        data={customers}
        rowKey={(customer) => customer.customerId}
      />

      {totalPages > 1 && (
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPrevious={onPrevious}
          onNext={onNext}
        />
      )}
    </section>
  );
}
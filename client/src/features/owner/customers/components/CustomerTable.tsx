import Card from "@/components/common/ui/Card/Card";
import SectionTitle from "@/components/common/ui/SectionTitle/SectionTitle";
import Button from "@/components/common/ui/Button/Button";

import DataTable, {
  type Column,
} from "@/components/common/ui/DataTable";

import StatusBadge from "@/components/common/ui/StatusBadge";
import Pagination from "@/components/common/ui/Pagination";

import type {
  CustomerResponse,
} from "../types/customer.types";

import {
  MessageSquareMore,
  UserCheck,
  UserPen,
  UserRoundPlus,
  UserX,
} from "lucide-react";

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
        <Card.Body className="py-16 text-center">
          Loading customers...
        </Card.Body>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <Card.Body className="py-16 text-center text-red-500">
          {error}
        </Card.Body>
      </Card>
    );
  }

  const columns: Column<CustomerResponse>[] = [
    {
      key: "fullName",
      header: "Customer",
      width: "340px",
      render: (customer) => (
        <div>
          <div className="flex items-center gap-2">
            <p className="shrink-0 font-semibold">
              {customer.fullName}
            </p>

            {customer.remarks?.trim() && (
              <span
                title={customer.remarks}
                className="inline-flex min-w-0 items-center gap-1 text-xs text-amber-600"
              >
                <MessageSquareMore
                  size={13}
                  className="shrink-0"
                />

                <span className="max-w-[160px] truncate">
                  {customer.remarks}
                </span>
              </span>
            )}
          </div>

          <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
            Joined {customer.joiningDate}
          </p>
        </div>
      ),
    },
    {
      key: "mobileNumber",
      header: "Mobile",
      width: "130px",
    },
    {
      key: "email",
      header: "Email",
      width: "200px",
      render: (customer) =>
        customer.email || "-",
    },
    {
      key: "status",
      header: "Status",
      width: "110px",
      headerClassName: "text-center",
      className: "text-center",
      render: (customer) =>
        customer.status === "PENDING" ? (
          <span className="inline-flex rounded-full bg-amber-100 px-2.5 py-1 text-xs font-semibold text-amber-800">
            PENDING
          </span>
        ) : (
          <StatusBadge
            label={customer.status}
            variant={
              customer.status === "ACTIVE"
                ? "success"
                : "danger"
            }
          />
        ),
    },
    {
      key: "actions",
      header: "Actions",
      width: "210px",
      headerClassName: "text-center",
      className: "text-center",
      render: (customer) => (
        <div className="flex justify-center gap-2">
          <Button
            type="button"
            size="sm"
            variant="secondary"
            className="p-2"
            title="Manage Customer"
            aria-label={`Manage ${customer.fullName}`}
            disabled={actionsDisabled}
            onClick={() => onEdit(customer)}
          >
            <UserPen size={16} />
          </Button>

          {customer.status === "PENDING" && (
            <Button
              type="button"
              size="sm"
              disabled={actionsDisabled}
              onClick={() =>
                onApprove(customer)
              }
            >
              <UserCheck size={16} />
              Approve
            </Button>
          )}

          {customer.status === "INACTIVE" && (
            <Button
              type="button"
              size="sm"
              variant="outline"
              disabled={actionsDisabled}
              onClick={() =>
                onReactivate(customer)
              }
            >
              <UserRoundPlus size={16} />
            </Button>
          )}

          {customer.status === "ACTIVE" && (
            <Button
              type="button"
              size="sm"
              variant="danger"
              className="p-2"
              title="Deactivate Customer"
              aria-label={`Deactivate ${customer.fullName}`}
              disabled={actionsDisabled}
              onClick={() =>
                onDeactivate(customer)
              }
            >
              <UserX size={16} />
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <Card className="interactive-surface">
      <Card.Header>
        <SectionTitle title="Customers" />
      </Card.Header>

      <Card.Body className="p-0">
        <DataTable
          columns={columns}
          data={customers}
          rowKey={(customer) =>
            customer.customerId
          }
          rowClassName={(customer) =>
            customer.status === "INACTIVE"
              ? "opacity-70"
              : ""
          }
        />
      </Card.Body>

      <Card.Footer>
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPrevious={onPrevious}
          onNext={onNext}
        />
      </Card.Footer>
    </Card>
  );
}
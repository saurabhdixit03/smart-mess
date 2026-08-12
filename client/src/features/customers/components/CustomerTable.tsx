
import Card from "@/components/common/ui/Card/Card";
import SectionTitle from "@/components/common/ui/SectionTitle/SectionTitle";
import Button from "@/components/common/ui/Button/Button";
import DataTable, {
  type Column,
} from "@/components/common/ui/DataTable";
import StatusBadge from "@/components/common/ui/StatusBadge";
import Pagination from "@/components/common/ui/Pagination";

import type { CustomerResponse } from "../types/customer.types";

import {
  MessageSquareMore,
  UserPen,
  UserX,
} from "lucide-react";

type CustomerTableProps = {
  customers: CustomerResponse[];
  loading: boolean;
  error: string | null;

  onEdit: (customer: CustomerResponse) => void;
  onDeactivate: (customer: CustomerResponse) => void;

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
  onDeactivate,
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
      key: "customerId",
      header: "Customer ID",
      width: "12%",
      className: "font-medium",
      render: (customer) => `#${customer.customerId}`,
    },

    {
      key: "fullName",
      header: "Customer",
      width: "25%",

      render: (customer) => (
        <div>
          <p className="font-semibold">
            {customer.fullName}
          </p>

          <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
            Joined {customer.joiningDate}
          </p>
        </div>
      ),
    },

    {
      key: "mobileNumber",
      header: "Mobile",
      width: "15%",
    },

    {
      key: "email",
      header: "Email",
      width: "20%",

      render: (customer) =>
        customer.email || "-",
    },

    {
      key: "remarks",
      header: "Remarks",
      width: "18%",

      render: (customer) =>
        customer.remarks ? (
          <div
            className="flex items-center gap-1 text-xs text-amber-600 truncate"
            title={customer.remarks}
          >
            <MessageSquareMore size={13} />
            {customer.remarks}
          </div>
        ) : (
          <span className="text-xs text-[var(--color-text-muted)]">
            No remarks
          </span>
        ),
    },

    {
      key: "status",
      header: "Status",
      width: "12%",

      headerClassName: "text-center",
      className: "text-center",

      render: (customer) => (
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
      width: "150px",

      headerClassName: "text-center",
      className: "text-center",

      render: (customer) => (
        <div className="flex justify-center gap-2">

          <Button
            size="sm"
            variant="secondary"
            className="p-2"
            title="Manage Customer"
            onClick={() => onEdit(customer)}
          >
            <UserPen size={16} />
          </Button>

          <Button
            size="sm"
            variant="danger"
            className="p-2"
            title={
              customer.status === "ACTIVE"
                ? "Deactivate Customer"
                : "Inactive Customer"
            }
            disabled={
              customer.status === "INACTIVE"
            }
            onClick={() =>
              onDeactivate(customer)
            }
          >
            <UserX size={16} />
          </Button>

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

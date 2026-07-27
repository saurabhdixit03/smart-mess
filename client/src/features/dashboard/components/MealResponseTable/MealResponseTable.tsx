import Card from "@/components/common/ui/Card/Card";
import DataTable, {
  type Column,
} from "@/components/common/ui/DataTable";
import StatusBadge from "@/components/common/ui/StatusBadge";

import type { DashboardCustomer } from "../../types/dashboard.types";

type MealResponseTableProps = {
  responses: DashboardCustomer[];
  loading: boolean;
  error: string | null;
};

export default function MealResponseTable({
  responses,
  loading,
  error,
}: MealResponseTableProps) {

  if (loading) {
    return (
      <Card>
        <Card.Body className="py-16 text-center">
          Loading meal responses...
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

  const columns: Column<DashboardCustomer>[] = [

    {
      key: "customerName",
      header: "Customer",
      width: "32%",

      render: (customer) => (
        <p className="font-semibold">
          {customer.customerName}
        </p>
      ),
    },

    {
      key: "responseStatus",
      header: "Response",
      width: "18%",

      headerClassName: "text-center",
      className: "text-center",

      render: (customer) => (
        <StatusBadge
          label={
            customer.responseStatus === "ACCEPTED"
              ? "Accepted"
              : "Declined"
          }
          variant={
            customer.responseStatus === "ACCEPTED"
              ? "success"
              : "danger"
          }
        />
      ),
    },

    {
      key: "mealOption",
      header: "Meal",
      width: "18%",

      headerClassName: "text-center",
      className: "text-center",

      render: (customer) => (
        <StatusBadge
          label={
            customer.mealOption === "FULL"
              ? "Full"
              : "Half"
          }
          variant={
            customer.mealOption === "FULL"
              ? "full"
              : "half"
          }
        />
      ),
    },

    {
      key: "extraRotiCount",
      header: "Extra Rotis",
      width: "14%",

      headerClassName: "text-center",
      className: "text-center",

      render: (customer) =>
        customer.extraRotiCount > 0
          ? customer.extraRotiCount
          : "—",
    },

    {
      key: "collected",
      header: "Collection",
      width: "18%",

      headerClassName: "text-center",
      className: "text-center",

      render: (customer) => (
        <StatusBadge
          label={
            customer.collected
              ? "Collected"
              : "Pending"
          }
          variant={
            customer.collected
              ? "success"
              : "warning"
          }
        />
      ),
    },

  ];

  return (
    <Card className="interactive-surface">

      <Card.Header>

        <h3 className="text-lg font-semibold">
          Collection Queue
        </h3>

      </Card.Header>

      <Card.Body className="p-0">

        <DataTable
          columns={columns}
          data={responses}
          rowKey={(customer) =>
            customer.mealResponseId
          }
        />

      </Card.Body>

    </Card>
  );
}
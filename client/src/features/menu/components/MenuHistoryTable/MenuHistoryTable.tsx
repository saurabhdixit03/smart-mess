import { useState } from "react";

import Card from "@/components/common/ui/Card/Card";

import DataTable, {
  type Column,
} from "@/components/common/ui/DataTable";

import Pagination from "@/components/common/ui/Pagination";

import SectionTitle from "@/components/common/ui/SectionTitle";

import StatusBadge from "@/components/common/ui/StatusBadge";

import { useMenuHistory } from "../../hooks/useMenuHistory";

import type { MenuResponse } from "../../types/menu.types";

export default function MenuHistoryTable() {

  const {
    menuHistory,
    loading,
    error,
  } = useMenuHistory();

  const [currentPage, setCurrentPage] =
    useState(1);

  const PAGE_SIZE = 10;

  const totalPages = Math.max(
    1,
    Math.ceil(menuHistory.length / PAGE_SIZE)
  );

  const paginatedMenus = menuHistory.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE
  );

  if (loading) {
    return (
      <Card className="mt-6">
        <Card.Body className="py-12 text-center">
          Loading menu history...
        </Card.Body>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className="mt-6">
        <Card.Body className="py-12 text-center text-red-500">
          {error}
        </Card.Body>
      </Card>
    );
  }

  if (menuHistory.length === 0) {
    return (
      <Card className="mt-6">
        <Card.Body className="py-12 text-center text-[var(--color-text-secondary)]">
          No menu history available.
        </Card.Body>
      </Card>
    );
  }

  const columns: Column<MenuResponse>[] = [
    {
      key: "menuDate",
      header: "Date",
      width: "18%",

      render: (menu) => {

        const formattedDate = new Date(
          menu.menuDate
        ).toLocaleDateString("en-IN", {
          day: "2-digit",
          month: "short",
          year: "numeric",
        });

        return (
          <p className="font-medium">
            {formattedDate}
          </p>
        );
      },
    },

    {
      key: "mealSession",
      header: "Session",
      width: "15%",

      headerClassName: "text-center",
      className: "text-center",

      render: (menu) => (

        <StatusBadge
          label={
            menu.mealSession === "LUNCH"
              ? "Lunch"
              : "Dinner"
          }
          variant={
            menu.mealSession === "LUNCH"
              ? "lunch"
              : "dinner"
          }
        />

      ),
    },

    {
      key: "menu",
      header: "Menu Details",

      render: (menu) => (

        <div className="space-y-2">

          <div className="flex flex-wrap gap-2">

            <span className="rounded-full bg-green-100 px-2 py-1 text-xs font-medium text-green-700">
              {menu.sabjiOne}
            </span>

            {menu.sabjiTwo && (
              <span className="rounded-full bg-green-100 px-2 py-1 text-xs font-medium text-green-700">
                {menu.sabjiTwo}
              </span>
            )}

          </div>

          <p className="text-sm text-[var(--color-text-secondary)]">
            {[menu.dal, menu.rice, menu.sweet]
              .filter(Boolean)
              .join(" • ")}
          </p>

        </div>

      ),
    },
  ];

  return (
    <Card className="mt-6 interactive-surface">

      <Card.Header className="space-y-1">

        <SectionTitle
          title="Menu History"
        />

        <p className="text-sm text-[var(--color-text-secondary)]">
          Previously published lunch and dinner menus.
        </p>

      </Card.Header>

      <Card.Body className="p-0">

        <DataTable
          columns={columns}
          data={paginatedMenus}
          rowKey={(menu) => menu.menuId}
        />

      </Card.Body>

      {totalPages > 1 && (

        <Card.Footer>

          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPrevious={() =>
              setCurrentPage((page) => page - 1)
            }
            onNext={() =>
              setCurrentPage((page) => page + 1)
            }
          />

        </Card.Footer>

      )}

    </Card>
  );
}
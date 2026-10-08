import { useState } from "react";

import Card from "@/components/common/ui/Card/Card";

import DataTable, {
  type Column,
} from "@/components/common/ui/DataTable";

import Pagination from "@/components/common/ui/Pagination";

import { useMenuHistory } from "../../hooks/useMenuHistory";

import type { MenuResponse } from "../../types/menu.types";

const PAGE_SIZE = 10;

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

export default function MenuHistoryTable() {
  const {
    menuHistory,
    loading,
    error,
  } = useMenuHistory();

  const [currentPage, setCurrentPage] = useState(1);

  const totalPages = Math.max(
    1,
    Math.ceil(menuHistory.length / PAGE_SIZE)
  );

  const visiblePage = Math.min(currentPage, totalPages);

  const paginatedMenus = menuHistory.slice(
    (visiblePage - 1) * PAGE_SIZE,
    visiblePage * PAGE_SIZE
  );

  const columns: Column<MenuResponse>[] = [
    {
      key: "menuDate",
      header: "Date",
      className: "whitespace-nowrap",
      render: (menu) => (
        <span className="font-medium">
          {formatDate(menu.menuDate)}
        </span>
      ),
    },
    {
      key: "mealSession",
      header: "Session",
      headerClassName: "text-center",
      className: "text-center",
      render: (menu) => (
        <span>
          {menu.mealSession === "LUNCH" ? "Lunch" : "Dinner"}
        </span>
      ),
    },
    {
      key: "sabjiOne",
      header: "Sabji",
      render: (menu) => (
        <span className="font-medium">
          {[menu.sabjiOne, menu.sabjiTwo]
            .filter(Boolean)
            .join(" · ")}
        </span>
      ),
    },
    {
      key: "dal",
      header: "Dal",
      render: (menu) => menu.dal || "—",
    },
    {
      key: "rice",
      header: "Rice",
      render: (menu) => menu.rice || "—",
    },
    {
      key: "sweet",
      header: "Sweet",
      render: (menu) => menu.sweet || "—",
    },
  ];

  return (
    <section className="mt-6 space-y-3">

      {loading ? (
        <Card>
          <Card.Body className="py-12 text-center text-[var(--color-text-secondary)]">
            <p role="status">Loading menu history...</p>
          </Card.Body>
        </Card>
      ) : error ? (
        <Card>
          <Card.Body className="py-12 text-center text-[var(--color-danger)]">
            <p role="alert">{error}</p>
          </Card.Body>
        </Card>
      ) : (
        <>
          <DataTable
            columns={columns}
            data={paginatedMenus}
            rowKey={(menu) => menu.menuId}
          />

          {totalPages > 1 && (
            <Pagination
              currentPage={visiblePage}
              totalPages={totalPages}
              onPrevious={() =>
                setCurrentPage(Math.max(1, visiblePage - 1))
              }
              onNext={() =>
                setCurrentPage(
                  Math.min(totalPages, visiblePage + 1)
                )
              }
            />
          )}
        </>
      )}
    </section>
  );
}
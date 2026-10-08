import { useMemo, useState } from "react";

import { Search } from "lucide-react";

import {
  Button,
  Card,
  Input,
  PageHeader,
} from "@/components/common/ui";

import Pagination from "@/components/common/ui/Pagination";

import MealRecordTable from "../components/MealRecordTable";

import { useMealRecords } from "../hooks";

import { getCustomer } from "@/features/auth/utils/auth.utils";

const PAGE_SIZE = 10;

type SessionFilter = "ALL" | "LUNCH" | "DINNER";

export default function MyMealsPage() {
  const customer = getCustomer();

  if (!customer) {
    return (
      <p
        role="alert"
        className="text-sm text-[var(--color-danger)]"
      >
        Customer session not found.
      </p>
    );
  }

  return (
    <CustomerMealHistory
      key={customer.customerId}
      customerId={customer.customerId}
    />
  );
}

function CustomerMealHistory({
  customerId,
}: {
  customerId: number;
}) {
  const {
    mealRecords,
    loading,
    error,
    fetchMealRecords,
  } = useMealRecords(customerId);

  const [search, setSearch] = useState("");

  const [sessionFilter, setSessionFilter] =
    useState<SessionFilter>("ALL");

  const [currentPage, setCurrentPage] = useState(1);

  const filteredMealRecords = useMemo(() => {
    const query = search.trim().toLowerCase();

    return mealRecords
      .filter((record) => {
        if (
          sessionFilter !== "ALL" &&
          record.mealSession !== sessionFilter
        ) {
          return false;
        }

        if (!query) {
          return true;
        }

        const date = new Date(record.collectedAt);

        const formattedDate = Number.isNaN(date.getTime())
          ? ""
          : date.toLocaleDateString("en-IN", {
              timeZone: "Asia/Kolkata",
              day: "2-digit",
              month: "short",
              year: "numeric",
            });

        const searchableText = [
          formattedDate,
          record.collectedAt,
          record.mealSession,
          record.mealOption,
        ]
          .join(" ")
          .toLowerCase();

        return searchableText.includes(query);
      })
      .sort(
        (first, second) =>
          second.collectedAt.localeCompare(
            first.collectedAt
          ) ||
          second.mealRecordId - first.mealRecordId
      );
  }, [mealRecords, search, sessionFilter]);

  const totalPages = Math.max(
    1,
    Math.ceil(filteredMealRecords.length / PAGE_SIZE)
  );

  const activePage = Math.min(currentPage, totalPages);

  const paginatedRecords = filteredMealRecords.slice(
    (activePage - 1) * PAGE_SIZE,
    activePage * PAGE_SIZE
  );

  return (
    <section className="min-w-0 space-y-4">
      <PageHeader
        title="My Meals"
        description="View your collected meals and recorded charges."
      />

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="w-full sm:max-w-sm">
          <Input
            fullWidth
            aria-label="Search meal history"
            placeholder="Search date, session or meal type..."
            value={search}
            onChange={(event) => {
              setSearch(event.target.value);
              setCurrentPage(1);
            }}
            leftIcon={<Search size={18} />}
          />
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {!loading && !error && (
            <p className="text-sm text-[var(--color-text-secondary)]">
              Showing{" "}
              <span className="font-medium text-[var(--color-text)]">
                {filteredMealRecords.length}
              </span>{" "}
              of {mealRecords.length} meals
            </p>
          )}

          <select
            aria-label="Meal session"
            value={sessionFilter}
            onChange={(event) => {
              setSessionFilter(
                event.target.value as SessionFilter
              );
              setCurrentPage(1);
            }}
            className="h-10 rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] px-3 text-sm text-[var(--color-text)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]"
          >
            <option value="ALL">All sessions</option>
            <option value="LUNCH">Lunch</option>
            <option value="DINNER">Dinner</option>
          </select>
        </div>
      </div>

      {loading ? (
        <Card>
          <Card.Body className="py-10 text-center">
            <p
              role="status"
              className="text-sm text-[var(--color-text-secondary)]"
            >
              Loading meal history...
            </p>
          </Card.Body>
        </Card>
      ) : error ? (
        <Card>
          <Card.Body className="space-y-3 py-8 text-center">
            <p
              role="alert"
              className="text-sm text-[var(--color-danger)]"
            >
              {error}
            </p>

            <Button
              type="button"
              variant="secondary"
              size="sm"
              onClick={() => void fetchMealRecords()}
            >
              Retry
            </Button>
          </Card.Body>
        </Card>
      ) : filteredMealRecords.length === 0 ? (
        <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-8 text-center text-sm text-[var(--color-text-secondary)]">
          {search.trim() || sessionFilter !== "ALL"
            ? "No meals match your filters."
            : "You haven't collected any meals yet."}
        </div>
      ) : (
        <div className="space-y-3">
          <MealRecordTable
            mealRecords={paginatedRecords}
          />

          {totalPages > 1 && (
            <Pagination
              currentPage={activePage}
              totalPages={totalPages}
              onPrevious={() =>
                setCurrentPage(
                  Math.max(1, activePage - 1)
                )
              }
              onNext={() =>
                setCurrentPage(
                  Math.min(totalPages, activePage + 1)
                )
              }
            />
          )}

          <p className="text-xs leading-5 text-[var(--color-text-secondary)]">
            Total includes the meal price and any extra
            rotis. Charges reflect prices recorded at
            collection.
          </p>
        </div>
      )}
    </section>
  );
}
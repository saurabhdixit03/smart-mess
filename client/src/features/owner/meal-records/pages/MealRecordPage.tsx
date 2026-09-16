import {
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  useNavigate,
} from "react-router-dom";

import {
  Search,
} from "lucide-react";

import Button from "@/components/common/ui/Button/Button";
import Input from "@/components/common/ui/Input/Input";
import PageHeader from "@/components/common/ui/PageHeader";
import Pagination from "@/components/common/ui/Pagination/Pagination";
import SearchToolbar from "@/components/common/ui/SearchToolbar/SearchToolbar";
import Select from "@/components/common/ui/Select/Select";

import { useMenus } from "@/features/owner/menu/hooks/useMenus";

import MealRecordQueue from "../components/MealRecordQueue";
import MealRecordSummary from "../components/MealRecordSummary";
import MealRecordTable from "../components/MealRecordTable";
import RecordMealDialog from "../components/RecordMealDialog";

import {
  useRecordQueue,
  useTodayMealRecords,
} from "../hooks";

import type {
  CollectionQueueItem,
} from "../types";

type MealSession =
  | "LUNCH"
  | "DINNER";

const MEAL_SESSIONS: MealSession[] = [
  "LUNCH",
  "DINNER",
];

export default function MealRecordPage() {
  const navigate = useNavigate();

  const {
    todayMenus,
    loading: menusLoading,
    error: menusError,
  } = useMenus();

  const [
    selectedSession,
    setSelectedSession,
  ] = useState<MealSession>(
    "LUNCH"
  );

  const [
    rowsPerPage,
    setRowsPerPage,
  ] = useState(10);

  const [
    currentPage,
    setCurrentPage,
  ] = useState(1);

  const [
    selectedCustomer,
    setSelectedCustomer,
  ] =
    useState<CollectionQueueItem | null>(
      null
    );

  const [
    search,
    setSearch,
  ] = useState("");

  const availableSessions =
    MEAL_SESSIONS.filter(
      (session) =>
        todayMenus.some(
          (menu) =>
            menu.mealSession ===
            session
        )
    );

  useEffect(() => {
    if (todayMenus.length === 0) {
      return;
    }

    const selectedMenuExists =
      todayMenus.some(
        (menu) =>
          menu.mealSession ===
          selectedSession
      );

    if (selectedMenuExists) {
      return;
    }

    const lunchAvailable =
      todayMenus.some(
        (menu) =>
          menu.mealSession ===
          "LUNCH"
      );

    setSelectedSession(
      lunchAvailable
        ? "LUNCH"
        : "DINNER"
    );
  }, [
    todayMenus,
    selectedSession,
  ]);

  useEffect(() => {
    setCurrentPage(1);
  }, [selectedSession]);

  const hasSelectedMenu =
    todayMenus.some(
      (menu) =>
        menu.mealSession ===
        selectedSession
    );

  const {
    recordQueue,
    loading,
    error,
    refetch,
  } = useRecordQueue(
    selectedSession,
    hasSelectedMenu &&
      !menusLoading
  );

  const {
    mealRecords,
    loading: historyLoading,
    error: historyError,
    refetch: refetchHistory,
  } = useTodayMealRecords(
    selectedSession,
    hasSelectedMenu &&
      !menusLoading
  );

  const pendingMeals =
    recordQueue.length;

  const fullMeals =
    recordQueue.filter(
      (item) =>
        item.mealOption === "FULL"
    ).length;

  const halfMeals =
    recordQueue.filter(
      (item) =>
        item.mealOption === "HALF"
    ).length;

  const filteredQueue =
    useMemo(() => {
      const query =
        search
          .trim()
          .toLowerCase();

      if (!query) {
        return recordQueue;
      }

      return recordQueue.filter(
        (item) =>
          item.customerName
            .toLowerCase()
            .includes(query)
      );
    }, [
      recordQueue,
      search,
    ]);

  const totalPages = Math.max(
    1,
    Math.ceil(
      mealRecords.length /
        rowsPerPage
    )
  );

  const paginatedRecords =
    mealRecords.slice(
      (currentPage - 1) *
        rowsPerPage,
      currentPage * rowsPerPage
    );

  if (
    menusLoading ||
    loading ||
    historyLoading
  ) {
    return (
      <div className="py-12 text-center">
        Loading meal records...
      </div>
    );
  }

  if (
    menusError ||
    error ||
    historyError
  ) {
    return (
      <div className="py-12 text-center text-red-500">
        {
          menusError ??
          error ??
          historyError
        }
      </div>
    );
  }

  if (todayMenus.length === 0) {
    return (
      <section className="space-y-6">

        <PageHeader
          title="Meal Collection"
          description="Record customer meal collections and review today's activity."
        />

        <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-8 text-center">

          <h2 className="text-lg font-semibold text-[var(--color-text)]">
            No menu published for today
          </h2>

          <p className="mx-auto mt-2 max-w-md text-sm text-[var(--color-text-secondary)]">
            Publish today's menu before starting meal collection.
          </p>

          <Button
            type="button"
            className="mt-6"
            onClick={() =>
              navigate("/owner/menu")
            }
          >
            Go to Menu
          </Button>

        </div>

      </section>
    );
  }

  return (
    <section className="space-y-6">

      <div
        className="
          flex
          flex-col
          gap-4
          sm:flex-row
          sm:items-center
          sm:justify-between
        "
      >
        <PageHeader
          title="Meal Collection"
          description="Record customer meal collections and review today's activity."
        />

        <div
          className="
            inline-flex
            self-end
            rounded-lg
            border
            border-[var(--color-border)]
            bg-[var(--color-surface)]
            p-1
            sm:self-auto
          "
        >
          {availableSessions.map(
            (session) => {
              const selected =
                session ===
                selectedSession;

              return (
                <button
                  key={session}
                  type="button"
                  onClick={() =>
                    setSelectedSession(
                      session
                    )
                  }
                  className={`
                    rounded-md
                    px-5
                    py-2
                    text-sm
                    font-medium
                    transition-all
                    duration-200
                    ${
                      selected
                        ? "bg-[var(--color-primary)] text-white shadow-sm"
                        : "text-[var(--color-text-secondary)] hover:bg-[var(--color-background)]"
                    }
                  `}
                >
                  {session === "LUNCH"
                    ? "Lunch"
                    : "Dinner"}
                </button>
              );
            }
          )}
        </div>

      </div>

      <MealRecordSummary
        pendingMeals={pendingMeals}
        fullMeals={fullMeals}
        halfMeals={halfMeals}
      />

      <SearchToolbar>

        <SearchToolbar.Left>

          <Input
            fullWidth
            inputSize="md"
            placeholder="Search pending customer..."
            value={search}
            onChange={(event) =>
              setSearch(
                event.target.value
              )
            }
            leftIcon={
              <Search size={18} />
            }
          />

        </SearchToolbar.Left>

        <SearchToolbar.Right>

          <div className="flex items-center gap-2">

            <span className="text-sm text-[var(--color-text-secondary)]">
              Rows
            </span>

            <Select
              value={String(
                rowsPerPage
              )}
              onChange={(event) => {
                setRowsPerPage(
                  Number(
                    event.target.value
                  )
                );

                setCurrentPage(1);
              }}
            >
              <option value="5">
                5
              </option>

              <option value="10">
                10
              </option>

              <option value="20">
                20
              </option>
            </Select>

          </div>

        </SearchToolbar.Right>

      </SearchToolbar>

      <p className="text-sm text-[var(--color-text-secondary)]">

        Showing{" "}

        <span className="font-semibold text-[var(--color-text)]">
          {filteredQueue.length}
        </span>{" "}

        customers waiting for meal collection
        {filteredQueue.length !== 1
          ? "s"
          : ""}

      </p>

      <MealRecordQueue
        items={filteredQueue}
        onRecord={
          setSelectedCustomer
        }
      />

      <MealRecordTable
        records={paginatedRecords}
      />

      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPrevious={() =>
          setCurrentPage(
            (page) =>
              Math.max(
                1,
                page - 1
              )
          )
        }
        onNext={() =>
          setCurrentPage(
            (page) =>
              Math.min(
                totalPages,
                page + 1
              )
          )
        }
      />

      <RecordMealDialog
        open={
          selectedCustomer !== null
        }
        customer={
          selectedCustomer
        }
        onClose={() =>
          setSelectedCustomer(null)
        }
        onSuccess={() => {
          refetch();

          refetchHistory();

          setSelectedCustomer(null);
        }}
      />

    </section>
  );
}
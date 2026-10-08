import {
  useId,
  useRef,
  useState,
} from "react";

import { useNavigate } from "react-router-dom";

import {
  ArrowRight,
  LoaderCircle,
  Search,
  X,
} from "lucide-react";

import Button from "@/components/common/ui/Button/Button";
import Input from "@/components/common/ui/Input/Input";
import PageHeader from "@/components/common/ui/PageHeader";
import Pagination from "@/components/common/ui/Pagination/Pagination";

import { useMenus } from "@/features/owner/menu/hooks/useMenus";

import MealRecordQueue from "../components/MealRecordQueue";
import MealRecordSummary from "../components/MealRecordSummary";
import MealRecordTable from "../components/MealRecordTable";
import RecordMealDialog from "../components/RecordMealDialog";

import {
  useRecordQueue,
  useTodayMealRecords,
} from "../hooks";

import { useCollectionCustomers } from "../hooks/useCollectionCustomers";

import type {
  CollectionCustomer,
  MealCollectionSelection,
  MealSession,
} from "../types";

const MEAL_SESSIONS: MealSession[] = [
  "LUNCH",
  "DINNER",
];

const RECORDS_PER_PAGE = 10;

export default function MealRecordPage() {
  const navigate = useNavigate();

  const searchInputRef =
    useRef<HTMLInputElement | null>(null);

  const searchResultsId = useId();

  const {
    todayMenus,
    loading: menusLoading,
    error: menusError,
  } = useMenus();

  const [selectedSession, setSelectedSession] =
    useState<MealSession>("LUNCH");

  const [currentPage, setCurrentPage] =
    useState(1);

  const [previousSession, setPreviousSession] =
    useState<MealSession>("LUNCH");

  const [selectedCustomer, setSelectedCustomer] =
    useState<MealCollectionSelection | null>(null);

  const [search, setSearch] = useState("");

  const availableSessions = MEAL_SESSIONS.filter(
    (session) =>
      todayMenus.some(
        (menu) => menu.mealSession === session
      )
  );

  const activeSession: MealSession =
    todayMenus.length === 0 ||
    availableSessions.includes(selectedSession)
      ? selectedSession
      : availableSessions.includes("LUNCH")
        ? "LUNCH"
        : "DINNER";

  const hasSelectedMenu =
    availableSessions.includes(activeSession);

  const selectedMenu = todayMenus.find(
    (menu) => menu.mealSession === activeSession
  );

  const {
    recordQueue,
    loading,
    error,
    refetch,
  } = useRecordQueue(
    activeSession,
    hasSelectedMenu && !menusLoading
  );

  const {
    mealRecords,
    loading: historyLoading,
    error: historyError,
    refetch: refetchHistory,
  } = useTodayMealRecords(
    activeSession,
    hasSelectedMenu && !menusLoading
  );

  const {
    customers: searchCustomers,
    loading: searchLoading,
    error: searchError,
    refresh: refreshSearch,
  } = useCollectionCustomers(
    activeSession,
    search,
    hasSelectedMenu && !menusLoading
  );

  const pendingMeals = recordQueue.length;

  const fullMeals = recordQueue.filter(
    (item) => item.mealOption === "FULL"
  ).length;

  const halfMeals = recordQueue.filter(
    (item) => item.mealOption === "HALF"
  ).length;

  const totalPages = Math.max(
    1,
    Math.ceil(
      mealRecords.length / RECORDS_PER_PAGE
    )
  );

  const activePage = Math.min(
    currentPage,
    totalPages
  );

  const paginatedRecords = mealRecords.slice(
    (activePage - 1) * RECORDS_PER_PAGE,
    activePage * RECORDS_PER_PAGE
  );

  if (selectedSession !== activeSession) {
    setSelectedSession(activeSession);
  }

  if (previousSession !== activeSession) {
    setPreviousSession(activeSession);
    setCurrentPage(1);
    setSelectedCustomer(null);
    setSearch("");
  }

  const showSearchResults =
    search.trim().length > 0;

  function selectSearchCustomer(
    customer: CollectionCustomer
  ) {
    if (
      customer.collected ||
      customer.menuId !== selectedMenu?.menuId
    ) {
      return;
    }

    setSelectedCustomer({
      customerId: customer.customerId,
      customerName: customer.customerName,
      menuId: customer.menuId,
      mealResponseId: customer.mealResponseId,
      mealOption: customer.mealOption,
      extraRotiCount: customer.extraRotiCount,
    });
  }

  function clearSearch() {
    setSearch("");
    searchInputRef.current?.focus();
  }

  function handleCollectionSaved() {
    setSelectedCustomer(null);
    refreshSearch();

    void refetch();
    void refetchHistory();
  }

  if (
    menusLoading ||
    loading ||
    historyLoading
  ) {
    return (
      <div
        role="status"
        className="py-12 text-center text-[var(--color-text-secondary)]"
      >
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
      <div
        role="alert"
        className="py-12 text-center text-[var(--color-danger)]"
      >
        {menusError ?? error ?? historyError}
      </div>
    );
  }

  if (todayMenus.length === 0) {
    return (
      <section className="space-y-4">
        <PageHeader
          title="Meal Collection"
          description="Record customer meal collections and review today's activity."
        />

        <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-8 text-center">
          <h2 className="text-lg font-semibold text-[var(--color-text)]">
            No menu published for today
          </h2>

          <p className="mx-auto mt-2 max-w-md text-sm text-[var(--color-text-secondary)]">
            Publish today's menu before starting
            meal collection.
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
    <section className="min-w-0 space-y-4">
      <PageHeader
        title="Meal Collection"
        description="Record customer meal collections and review today's activity."
        action={
          <div className="inline-flex rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] p-1">
            {availableSessions.map((session) => (
              <button
                key={session}
                type="button"
                aria-pressed={
                  session === activeSession
                }
                onClick={() =>
                  setSelectedSession(session)
                }
                className={`
                  rounded-md px-5 py-2
                  text-sm font-medium
                  transition-colors
                  focus-visible:outline-none
                  focus-visible:ring-2
                  focus-visible:ring-[var(--color-primary)]
                  ${
                    session === activeSession
                      ? "bg-[var(--color-primary)] text-white shadow-sm"
                      : "text-[var(--color-text-secondary)] hover:bg-[var(--color-background)]"
                  }
                `}
              >
                {session === "LUNCH"
                  ? "Lunch"
                  : "Dinner"}
              </button>
            ))}
          </div>
        }
      />

      <MealRecordSummary
        pendingMeals={pendingMeals}
        fullMeals={fullMeals}
        halfMeals={halfMeals}
      />

      <div className="flex min-w-0 flex-col gap-3 lg:flex-row lg:items-start">
        <div className="w-full shrink-0 sm:max-w-sm">
          <div className="relative">
            <Input
              ref={searchInputRef}
              fullWidth
              inputSize="md"
              aria-label="Search active customers by name or mobile"
              aria-controls={
                showSearchResults
                  ? searchResultsId
                  : undefined
              }
              placeholder="Search customer name or mobile..."
              value={search}
              maxLength={100}
              onChange={(event) =>
                setSearch(event.target.value)
              }
              onKeyDown={(event) => {
                if (event.key === "Escape") {
                  clearSearch();
                }
              }}
              leftIcon={<Search size={18} />}
              className="pr-10"
            />

            {search.length > 0 && (
              <button
                type="button"
                aria-label="Clear customer search"
                onClick={clearSearch}
                className="absolute right-3 top-1/2 flex h-6 w-6 -translate-y-1/2 items-center justify-center rounded-md text-[var(--color-text-secondary)] hover:text-[var(--color-text)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]"
              >
                <X
                  size={16}
                  aria-hidden="true"
                />
              </button>
            )}
          </div>
        </div>

        {showSearchResults && (
          <div
            id={searchResultsId}
            aria-label="Matching active customers"
            className="min-w-0 flex-1"
          >
            {searchLoading ? (
              <div
                role="status"
                className="flex min-h-11 items-center gap-2 text-sm text-[var(--color-text-secondary)]"
              >
                <LoaderCircle
                  size={16}
                  className="animate-spin"
                  aria-hidden="true"
                />
                Searching customers...
              </div>
            ) : searchError ? (
              <div className="flex min-h-11 flex-wrap items-center gap-3">
                <p
                  role="alert"
                  className="text-sm text-[var(--color-danger)]"
                >
                  {searchError}
                </p>

                <Button
                  type="button"
                  size="sm"
                  variant="secondary"
                  onClick={refreshSearch}
                >
                  Retry
                </Button>
              </div>
            ) : searchCustomers.length === 0 ? (
              <p
                role="status"
                className="flex min-h-11 items-center text-sm text-[var(--color-text-secondary)]"
              >
                No active customers match your search.
              </p>
            ) : (
              <div className="space-y-2">
                <ul className="flex gap-2 overflow-x-auto overscroll-x-contain pb-2">
                  {searchCustomers.map((customer) => {
                    const unavailable =
                      customer.collected ||
                      customer.menuId !==
                        selectedMenu?.menuId;

                    return (
                      <li
                        key={customer.customerId}
                        className="w-56 shrink-0"
                      >
                        <button
                          type="button"
                          disabled={unavailable}
                          onClick={() =>
                            selectSearchCustomer(
                              customer
                            )
                          }
                          className="flex w-full items-center justify-between gap-3 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2.5 text-left transition-colors hover:border-[var(--color-primary)] hover:bg-[var(--color-background)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-[var(--color-primary)] disabled:cursor-default disabled:opacity-60"
                        >
                          <span className="min-w-0">
                            <span
                              title={
                                customer.customerName
                              }
                              className="block truncate text-sm font-medium text-[var(--color-text)]"
                            >
                              {customer.customerName}
                            </span>

                          </span>

                          <span
                            className={`
                              flex shrink-0 items-center gap-1
                              text-xs font-medium
                              ${
                                unavailable
                                  ? "text-[var(--color-text-secondary)]"
                                  : "text-[var(--color-primary)]"
                              }
                            `}
                          >
                            {customer.collected
                              ? "Collected"
                              : unavailable
                                ? "Unavailable"
                                : "Record"}

                            {!unavailable && (
                              <ArrowRight
                                size={13}
                                aria-hidden="true"
                              />
                            )}
                          </span>
                        </button>
                      </li>
                    );
                  })}
                </ul>

                {searchCustomers.length === 50 && (
                  <p className="text-xs text-[var(--color-text-secondary)]">
                    Showing the first 50 matches.
                    Refine your search to find more
                    customers.
                  </p>
                )}
              </div>
            )}
          </div>
        )}
      </div>

      <MealRecordQueue
        items={recordQueue}
        onRecord={setSelectedCustomer}
      />

      <div className="space-y-3">
        <MealRecordTable
          records={paginatedRecords}
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
                Math.min(
                  totalPages,
                  activePage + 1
                )
              )
            }
          />
        )}
      </div>

      <RecordMealDialog
        open={selectedCustomer !== null}
        customer={selectedCustomer}
        onClose={() =>
          setSelectedCustomer(null)
        }
        onSuccess={handleCollectionSaved}
      />
    </section>
  );
}
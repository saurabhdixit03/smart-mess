import {
  useId,
  useRef,
  useState,
} from "react";

import { useNavigate } from "react-router-dom";

import {
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

const MEAL_SESSIONS: MealSession[] = ["LUNCH", "DINNER"];
const RECORDS_PER_PAGE = 10;

export default function MealRecordPage() {
  const navigate = useNavigate();
  const searchInputRef = useRef<HTMLInputElement | null>(null);
  const searchResultsId = useId();

  const {
    todayMenus,
    loading: menusLoading,
    error: menusError,
  } = useMenus();

  const [selectedSession, setSelectedSession] =
    useState<MealSession>("LUNCH");

  const [currentPage, setCurrentPage] = useState(1);

  const [previousSession, setPreviousSession] =
    useState<MealSession>("LUNCH");

  const [selectedCustomer, setSelectedCustomer] =
    useState<MealCollectionSelection | null>(null);

  const [search, setSearch] = useState("");
  const [searchOpen, setSearchOpen] = useState(false);

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
    Math.ceil(mealRecords.length / RECORDS_PER_PAGE)
  );

  const activePage = Math.min(currentPage, totalPages);

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
    setSearchOpen(false);
  }

  if (currentPage > totalPages) {
    setCurrentPage(totalPages);
  }

  const showSearchResults =
    searchOpen && search.trim().length > 0;

  function selectSearchCustomer(customer: CollectionCustomer) {
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

    setSearchOpen(false);
  }

  function clearSearch() {
    setSearch("");
    setSearchOpen(false);
    searchInputRef.current?.focus();
  }

  function handleCollectionSaved() {
    setSelectedCustomer(null);
    refreshSearch();

    void refetch();
    void refetchHistory();
  }

  if (menusLoading || loading || historyLoading) {
    return (
      <div className="py-12 text-center">
        Loading meal records...
      </div>
    );
  }

  if (menusError || error || historyError) {
    return (
      <div className="py-12 text-center text-red-500">
        {menusError ?? error ?? historyError}
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
            onClick={() => navigate("/owner/menu")}
          >
            Go to Menu
          </Button>
        </div>
      </section>
    );
  }

  return (
    <section className="min-w-0 space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <PageHeader
          title="Meal Collection"
          description="Record customer meal collections and review today's activity."
        />

        <div className="inline-flex self-end rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] p-1 sm:self-auto">
          {availableSessions.map((session) => (
            <button
              key={session}
              type="button"
              onClick={() => setSelectedSession(session)}
              className={`rounded-md px-5 py-2 text-sm font-medium transition-colors ${
                session === activeSession
                  ? "bg-[var(--color-primary)] text-white shadow-sm"
                  : "text-[var(--color-text-secondary)] hover:bg-[var(--color-background)]"
              }`}
            >
              {session === "LUNCH" ? "Lunch" : "Dinner"}
            </button>
          ))}
        </div>
      </div>

      <MealRecordSummary
        pendingMeals={pendingMeals}
        fullMeals={fullMeals}
        halfMeals={halfMeals}
      />

      <div
        className="relative w-full"
        onFocusCapture={() => setSearchOpen(true)}
        onBlurCapture={(event) => {
          if (
            !event.currentTarget.contains(
              event.relatedTarget as Node | null
            )
          ) {
            setSearchOpen(false);
          }
        }}
        onKeyDown={(event) => {
          if (event.key === "Escape") {
            searchInputRef.current?.focus();
            setSearchOpen(false);
          }
        }}
      >
        <div className="relative">
          <Input
            ref={searchInputRef}
            fullWidth
            inputSize="md"
            aria-label="Search active customers by name or mobile"
            aria-controls={
              showSearchResults ? searchResultsId : undefined
            }
            placeholder="Search customer name or mobile..."
            value={search}
            maxLength={100}
            onChange={(event) => {
              setSearch(event.target.value);
              setSearchOpen(true);
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
              <X size={16} />
            </button>
          )}
        </div>

        {showSearchResults && (
          <div
            id={searchResultsId}
            className="absolute left-0 right-0 top-full z-20 mt-2 overflow-hidden rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] shadow-lg"
          >
            <div className="border-b border-[var(--color-border)] px-4 py-2.5">
              <p className="text-xs font-medium text-[var(--color-text-secondary)]">
                Active customers ·{" "}
                {activeSession === "LUNCH" ? "Lunch" : "Dinner"}
              </p>
            </div>

            {searchLoading ? (
              <div
                role="status"
                className="flex items-center justify-center gap-2 px-4 py-6 text-sm text-[var(--color-text-secondary)]"
              >
                <LoaderCircle size={16} className="animate-spin" />
                Searching customers...
              </div>
            ) : searchError ? (
              <div className="space-y-3 px-4 py-4">
                <p role="alert" className="text-sm text-red-500">
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
                className="px-4 py-6 text-center text-sm text-[var(--color-text-secondary)]"
              >
                No active customers match your search.
              </p>
            ) : (
              <>
                <ul className="max-h-72 overflow-y-auto overscroll-contain divide-y divide-[var(--color-border)] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
                  {searchCustomers.map((customer) => (
                    <li key={customer.customerId}>
                      <button
                        type="button"
                        disabled={customer.collected}
                        onClick={() => selectSearchCustomer(customer)}
                        className="flex w-full items-center justify-between gap-3 px-4 py-3 text-left transition-colors hover:bg-[var(--color-surface-hover)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-[var(--color-primary)] disabled:cursor-default disabled:opacity-60"
                      >
                        <span className="min-w-0">
                          <span className="block truncate text-sm font-medium text-[var(--color-text)]">
                            {customer.customerName}
                          </span>

                          <span className="mt-0.5 block text-xs text-[var(--color-text-secondary)]">
                            {customer.mobileNumber}
                          </span>
                        </span>

                        <span
                          className={`shrink-0 text-xs font-medium ${
                            customer.collected
                              ? "text-[var(--color-text-secondary)]"
                              : "text-[var(--color-primary)]"
                          }`}
                        >
                          {customer.collected ? "Collected" : "Record"}
                        </span>
                      </button>
                    </li>
                  ))}
                </ul>

                {searchCustomers.length === 50 && (
                  <p className="border-t border-[var(--color-border)] px-4 py-2 text-xs text-[var(--color-text-secondary)]">
                    Showing the first 50 matches. Refine your search
                    to find more customers.
                  </p>
                )}
              </>
            )}
          </div>
        )}
      </div>

      <div className="space-y-3">


        <MealRecordQueue
          items={recordQueue}
          onRecord={setSelectedCustomer}
        />
      </div>

      <div className="space-y-4">
        <MealRecordTable records={paginatedRecords} />

        {totalPages > 1 && (
          <Pagination
            currentPage={activePage}
            totalPages={totalPages}
            onPrevious={() =>
              setCurrentPage((page) => Math.max(1, page - 1))
            }
            onNext={() =>
              setCurrentPage((page) =>
                Math.min(totalPages, page + 1)
              )
            }
          />
        )}
      </div>

      <RecordMealDialog
        open={selectedCustomer !== null}
        customer={selectedCustomer}
        onClose={() => setSelectedCustomer(null)}
        onSuccess={handleCollectionSaved}
      />
    </section>
  );
}
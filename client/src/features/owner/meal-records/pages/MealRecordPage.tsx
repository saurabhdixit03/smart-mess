import { useEffect, useState, useMemo } from "react";

import MealSessionSelector from "@/components/common/business/MealSessionSelector/MealSessionSelector";
import { useMenus } from "@/features/menu/hooks/useMenus";

import { useRecordQueue } from "../hooks";

import MealRecordQueue from "../components/MealRecordQueue";
import RecordMealDialog from "../components/RecordMealDialog";

import MealRecordSummary from "../components/MealRecordSummary";

import type { CollectionQueueItem } from "../types";

import { useTodayMealRecords } from "../hooks";

import MealRecordTable from "../components/MealRecordTable";



import Input from "@/components/common/ui/Input/Input";
import SearchToolbar from "@/components/common/ui/SearchToolbar/SearchToolbar";
import { Search } from "lucide-react";

import Pagination from "@/components/common/ui/Pagination/Pagination";
import Select from "@/components/common/ui/Select/Select";

export default function MealRecordPage() {

  const {
    todayMenus,
    loading: menusLoading,
    error: menusError,
  } = useMenus();

  const [
  selectedSession,
  setSelectedSession,
] = useState<"LUNCH" | "DINNER">(() => {

  const savedSession =
    localStorage.getItem(
      "meal-record-session"
    );

  return savedSession === "DINNER"
    ? "DINNER"
    : "LUNCH";

});

const [rowsPerPage, setRowsPerPage] =
  useState(10);

const [currentPage, setCurrentPage] =
  useState(1);

useEffect(() => {

  localStorage.setItem(
    "meal-record-session",
    selectedSession
  );

}, [selectedSession]);

  const {
    recordQueue,
    loading,
    error,
    refetch,
  } = useRecordQueue(selectedSession);

  const [
    selectedCustomer,
    setSelectedCustomer,
  ] = useState<CollectionQueueItem | null>(null);

  const [
  search,
  setSearch,
] = useState("");

  const pendingMeals = recordQueue.length;

const fullMeals = recordQueue.filter(
  (item) => item.mealOption === "FULL"
).length;

const halfMeals = recordQueue.filter(
  (item) => item.mealOption === "HALF"
).length;

const filteredQueue = useMemo(() => {

  return recordQueue.filter((item) =>
    item.customerName
      .toLowerCase()
      .includes(search.toLowerCase())
  );

}, [recordQueue, search]);

const {
  mealRecords,
  loading: historyLoading,
  error: historyError,
  refetch: refetchHistory,
} = useTodayMealRecords(
  selectedSession
);

const totalPages = Math.max(
  1,
  Math.ceil(
    mealRecords.length / rowsPerPage
  )
);

const paginatedRecords =
  mealRecords.slice(
    (currentPage - 1) * rowsPerPage,
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
        {menusError ?? error ?? historyError}
      </div>
    );

  }

  return (

    <section className="space-y-6">

      <MealSessionSelector
        menus={todayMenus}
        selectedSession={selectedSession}
        onSelect={setSelectedSession}
        title="Meal Collection"
        variant="toggle"
      />

  

<MealRecordSummary
  pendingMeals={pendingMeals}
  fullMeals={fullMeals}
  halfMeals={halfMeals}
/>

{/* ---------- Search Card ---------- */}

<SearchToolbar>

  <SearchToolbar.Left>

    <Input
      fullWidth
      inputSize="md"
      placeholder="Search pending customer..."
      value={search}
      onChange={(event) =>
        setSearch(event.target.value)
      }
      leftIcon={<Search size={18} />}
    />

  </SearchToolbar.Left>

  <SearchToolbar.Right>

    <div className="flex items-center gap-2">

      <span className="text-sm text-[var(--color-text-secondary)]">
        Rows
      </span>

      <Select
        value={String(rowsPerPage)}
        onChange={(event) => {

          setRowsPerPage(
            Number(event.target.value)
          );

          setCurrentPage(1);

        }}
      >

        <option value="5">5</option>

        <option value="10">10</option>

        <option value="20">20</option>

      </Select>

    </div>

  </SearchToolbar.Right>

</SearchToolbar>

<p className="mt-3 text-sm text-[var(--color-text-secondary)]">

  Showing{" "}

  <span className="font-semibold text-[var(--color-text)]">
    {filteredQueue.length}
  </span>{" "}

  customers waiting for meal collection
  {filteredQueue.length !== 1 ? "s" : ""}

</p>

{/* ---------- Pending Queue ---------- */}

<MealRecordQueue
  items={filteredQueue}
  onRecord={setSelectedCustomer}
/>

{/* ---------- Today's Meal Records ---------- */}

<MealRecordTable
  records={paginatedRecords}
/>

<Pagination
  currentPage={currentPage}
  totalPages={totalPages}
  onPrevious={() =>
    setCurrentPage((page) =>
      Math.max(1, page - 1)
    )
  }
  onNext={() =>
    setCurrentPage((page) =>
      Math.min(
        totalPages,
        page + 1
      )
    )
  }
/>
      <RecordMealDialog
  open={selectedCustomer !== null}
  customer={selectedCustomer}
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
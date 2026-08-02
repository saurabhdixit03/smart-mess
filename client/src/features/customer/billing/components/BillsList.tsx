import { useState } from "react";

import BillCard from "./BillCard";
import BillDetailsModal from "./BillDetailsModal";

import type { Bill } from "../types";

interface BillsListProps {
  bills: Bill[];
}

export default function BillsList({
  bills,
}: BillsListProps) {
  const [selectedBillId, setSelectedBillId] =
    useState<number | null>(null);

  const [modalOpen, setModalOpen] =
    useState(false);

  function handleOpen(
    billId: number
  ) {
    setSelectedBillId(billId);
    setModalOpen(true);
  }

  function handleClose() {
    setModalOpen(false);
    setSelectedBillId(null);
  }

  return (
    <>
      <div
        className="
          grid
          gap-5

          sm:grid-cols-2
          xl:grid-cols-3
        "
      >
        {bills.map((bill) => (
          <BillCard
  key={bill.billId}
  bill={bill}
  onView={handleOpen}
/>
        ))}
      </div>

      <BillDetailsModal
        billId={selectedBillId}
        open={modalOpen}
        onClose={handleClose}
      />
    </>
  );
}
import { useState } from "react";

import BillCard from "./BillCard";
import BillDetailsModal from "./BillDetailsModal";
import PaymentModal from "./PaymentModal";

import type { Bill } from "../types";

interface BillsListProps {
  bills: Bill[];
}

export default function BillsList({
  bills,
}: BillsListProps) {
  const [selectedBillId, setSelectedBillId] =
    useState<number | null>(null);

  const [detailsModalOpen, setDetailsModalOpen] =
    useState(false);

  const [selectedPaymentBillId, setSelectedPaymentBillId] =
    useState<number | null>(null);

  const [paymentModalOpen, setPaymentModalOpen] =
    useState(false);

  function handleView(
    billId: number
  ) {
    setSelectedBillId(billId);
    setDetailsModalOpen(true);
  }

  function handleCloseDetails() {
    setDetailsModalOpen(false);
    setSelectedBillId(null);
  }

  function handlePay(
    billId: number
  ) {
    setSelectedPaymentBillId(billId);
    setPaymentModalOpen(true);
  }

  function handleClosePayment() {
    setPaymentModalOpen(false);
    setSelectedPaymentBillId(null);
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
            onView={handleView}
            onPay={handlePay}
          />
        ))}
      </div>

      <BillDetailsModal
        billId={selectedBillId}
        open={detailsModalOpen}
        onClose={handleCloseDetails}
      />

      <PaymentModal
        billId={selectedPaymentBillId}
        open={paymentModalOpen}
        onClose={handleClosePayment}
      />
    </>
  );
}
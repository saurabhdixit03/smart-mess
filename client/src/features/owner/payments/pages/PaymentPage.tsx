import { useState } from "react";

import { toast } from "sonner";

import Button from "@/components/common/ui/Button/Button";
import Modal from "@/components/common/ui/Modal/Modal";
import PageHeader from "@/components/common/ui/PageHeader";

import {
  PaymentStats,
  PendingPaymentsTable,
  UnpaidBillsTable,
} from "../components";

import { paymentApi } from "../api";

import { usePayments } from "../hooks";

import type {
  BillResponse,
  PaymentMode,
  PendingPaymentResponse,
} from "../types";

export default function PaymentPage() {

  const {
    overview,
    loading,
    refreshPayments,
  } = usePayments();

  const [selectedBill, setSelectedBill] =
    useState<BillResponse | null>(null);

  const [selectedPendingPayment, setSelectedPendingPayment] =
    useState<PendingPaymentResponse | null>(null);

  // ------------------------------------
  // Cash Payment
  // ------------------------------------

  const handleCollectCash =
    async () => {

      if (!selectedBill) {
        return;
      }

      try {

        await paymentApi.collectPayment({

          billId: selectedBill.billId,

          paymentMode: "CASH" as PaymentMode,

        });

        toast.success(
          "Cash payment collected successfully."
        );

        setSelectedBill(null);

        await refreshPayments();

      } catch (error) {

        console.error(error);

        toast.error(
          "Failed to collect payment."
        );

      }

    };

  // ------------------------------------
  // Approve UPI Payment
  // ------------------------------------

  const handleApprovePayment =
    async () => {

      if (!selectedPendingPayment) {
        return;
      }

      try {

        await paymentApi.collectPayment({

          billId:
            selectedPendingPayment.billId,

          paymentMode: "UPI" as PaymentMode,

        });

        toast.success(
          "UPI payment approved successfully."
        );

        setSelectedPendingPayment(null);

        await refreshPayments();

      } catch (error) {

        console.error(error);

        toast.error(
          "Failed to approve payment."
        );

      }

    };

  if (loading || !overview) {
    return (
      <div className="p-6">
        Loading payments...
      </div>
    );
  }

  return (
    <>

      <div className="space-y-6">

        <PageHeader
          title="Payments"
          description="Manage bill payments and UPI requests."
        />

        <PaymentStats
          overview={overview}
        />

        <UnpaidBillsTable
          bills={overview.unpaidBills}
          onCollectCash={
            setSelectedBill
          }
        />

        <PendingPaymentsTable
          pendingPayments={
            overview.pendingPayments
          }
          onApprovePayment={
            setSelectedPendingPayment
          }
        />

      </div>

      {/* -----------------------------
          Cash Payment Modal
      ------------------------------ */}

      <Modal
        open={selectedBill !== null}
        title="Collect Cash Payment"
        onClose={() =>
          setSelectedBill(null)
        }
        footer={
          <>
            <Button
              variant="secondary"
              onClick={() =>
                setSelectedBill(null)
              }
            >
              Cancel
            </Button>

            <Button
              onClick={
                handleCollectCash
              }
            >
              Collect Payment
            </Button>
          </>
        }
      >

        <p>

          Are you sure you want to collect cash payment from{" "}

          <strong>
            {selectedBill?.customerName}
          </strong>

          ?

        </p>

        <p className="mt-3 text-sm text-[var(--color-text-secondary)]">

          Amount:{" "}

          <strong>

            ₹
            {selectedBill
              ? Number(
                  selectedBill.totalAmount
                ).toLocaleString("en-IN")
              : 0}

          </strong>

        </p>

      </Modal>

      {/* -----------------------------
          UPI Approval Modal
      ------------------------------ */}

      <Modal
        open={
          selectedPendingPayment !== null
        }
        title="Approve UPI Payment"
        onClose={() =>
          setSelectedPendingPayment(
            null
          )
        }
        footer={
          <>
            <Button
              variant="secondary"
              onClick={() =>
                setSelectedPendingPayment(
                  null
                )
              }
            >
              Cancel
            </Button>

            <Button
              onClick={
                handleApprovePayment
              }
            >
              Approve Payment
            </Button>
          </>
        }
      >

        <p>

          Are you sure you want to approve the UPI payment from{" "}

          <strong>
            {selectedPendingPayment?.customerName}
          </strong>

          ?

        </p>

        <p className="mt-3 text-sm text-[var(--color-text-secondary)]">

          Amount:{" "}

          <strong>

            ₹
            {selectedPendingPayment
              ? Number(
                  selectedPendingPayment.totalAmount
                ).toLocaleString("en-IN")
              : 0}

          </strong>

        </p>

      </Modal>

    </>
  );

}
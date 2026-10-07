import { useEffect, useRef } from "react";
import { ExternalLink, RefreshCw } from "lucide-react";

import { Button, Modal } from "@/components/common/ui";

import { usePaymentCheckout } from "../hooks";
import type { PaymentOrderStatus } from "../types";

interface PaymentModalProps {
  billId: number | null;
  open: boolean;
  onClose: () => void;
  onPaid?: () => void;
}

const STATUS_MESSAGES: Record<PaymentOrderStatus, string> = {
  CREATING: "Your checkout is being prepared. Check its status shortly.",
  ACTIVE: "Open checkout to choose your payment method.",
  PAID: "Payment confirmed. Your bill is paid.",
  EXPIRED: "This checkout has expired. You can request a new checkout.",
  TERMINATION_REQUESTED:
    "This checkout is closing. Check its status shortly.",
  TERMINATED: "This checkout has closed. You can request a new checkout.",
  CREATION_FAILED:
    "Checkout could not be created. You can try again.",
  RECONCILIATION_REQUIRED:
    "We are checking this payment. Please check its status before trying again.",
};

const RETRY_STATUSES: PaymentOrderStatus[] = [
  "EXPIRED",
  "TERMINATED",
  "CREATION_FAILED",
];

const currencyFormat = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
});

export default function PaymentModal({
  billId,
  open,
  onClose,
  onPaid,
}: PaymentModalProps) {
  const {
    payment,
    loading,
    submitting,
    error,
    fetchCheckout,
    verifyPayment,
    openCheckout,
    reset,
  } = usePaymentCheckout();

  const notifiedOrderId = useRef<number | null>(null);

  useEffect(() => {
    if (open && billId !== null) {
      void fetchCheckout(billId);
    } else {
      reset();
      notifiedOrderId.current = null;
    }
  }, [open, billId, fetchCheckout, reset]);

  useEffect(() => {
    if (
      !open ||
      !payment ||
      payment.status !== "PAID" ||
      notifiedOrderId.current === payment.paymentOrderId
    ) {
      return;
    }

    notifiedOrderId.current = payment.paymentOrderId;

    onPaid?.();
    onClose();
  }, [open, payment, onPaid, onClose]);

  const canOpenCheckout =
    payment?.status === "ACTIVE" &&
    Boolean(payment.paymentSessionId);

  const canRequestNewCheckout =
    payment !== null &&
    RETRY_STATUSES.includes(payment.status);

  function handleClose() {
    if (!submitting) {
      onClose();
    }
  }

  function handleRetry() {
    if (billId !== null && !loading && !submitting) {
      void fetchCheckout(billId);
    }
  }

  return (
    <Modal
      open={open}
      onClose={handleClose}
      title="Pay Bill"
      size="md"
      footer={
        <>
          <Button
            variant="secondary"
            disabled={submitting}
            onClick={handleClose}
          >
            Close
          </Button>

          {payment && payment.status !== "PAID" && (
            <Button
              disabled={loading || submitting}
              onClick={() => void verifyPayment()}
            >
              <RefreshCw size={16} />
              {submitting ? "Checking..." : "Check Payment Status"}
            </Button>
          )}
        </>
      }
    >
      <div className="space-y-5">
        {loading && (
          <div className="py-10 text-center">
            Preparing checkout...
          </div>
        )}

        {!loading && payment && (
          <>
            {payment.environment === "SANDBOX" && (
              <div className="rounded-lg border border-amber-300 bg-amber-50 p-3 text-sm text-amber-900">
                Test payment — no real money is charged.
              </div>
            )}

            <div className="rounded-xl bg-[var(--color-surface-secondary)] py-5 text-center">
              <p className="text-sm text-[var(--color-text-secondary)]">
                Bill #{payment.billId}
              </p>

              <h2 className="mt-2 text-4xl font-bold tracking-tight">
                {currencyFormat.format(payment.amount)}
              </h2>
            </div>

            <p
              className="text-center text-sm text-[var(--color-text-secondary)]"
              aria-live="polite"
            >
              {STATUS_MESSAGES[payment.status]}
            </p>

            {canOpenCheckout && (
              <Button
                fullWidth
                disabled={submitting}
                onClick={() => void openCheckout()}
              >
                <ExternalLink size={18} />
                {submitting
                  ? "Processing..."
                  : payment.environment === "SANDBOX"
                    ? "Open Test Checkout"
                    : "Pay Securely"}
              </Button>
            )}

            {canRequestNewCheckout && (
              <Button
                fullWidth
                disabled={submitting}
                onClick={handleRetry}
              >
                Try New Checkout
              </Button>
            )}
          </>
        )}

        {error && (
          <p
            role="alert"
            className="text-center text-sm text-red-500"
          >
            {error}
          </p>
        )}

        {!loading && !payment && error && (
          <Button
            fullWidth
            disabled={submitting}
            onClick={handleRetry}
          >
            Retry
          </Button>
        )}
      </div>
    </Modal>
  );
}
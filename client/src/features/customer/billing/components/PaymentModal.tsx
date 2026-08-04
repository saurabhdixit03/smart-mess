import { useEffect } from "react";
import { ExternalLink } from "lucide-react";
import QRCode from "react-qr-code";

import {
  Button,
  //CopyButton,
  Modal,
} from "@/components/common/ui";

import { useUpiPayment } from "../hooks";

interface PaymentModalProps {
  billId: number | null;
  open: boolean;
  onClose: () => void;
}

export default function PaymentModal({
  billId,
  open,
  onClose,
}: PaymentModalProps) {
  const {
    payment,
    loading,
    submitting,
    error,
    fetchUpiPayment,
    requestVerification,
  } = useUpiPayment();

  useEffect(() => {
    if (open && billId) {
      fetchUpiPayment(billId);
    }
  }, [open, billId, fetchUpiPayment]);

  async function handleCompleted() {
    if (!billId) return;

    const success =
      await requestVerification(billId);

    if (success) {
      onClose();
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Pay Bill"
      size="md"
      footer={
        <>
          <Button
            variant="secondary"
            onClick={onClose}
          >
            Close
          </Button>

          <Button
            disabled={
              loading ||
              submitting ||
              !payment
            }
            onClick={handleCompleted}
          >
            {submitting
              ? "Submitting..."
              : "I've Completed Payment"}
          </Button>
        </>
      }
    >
      {loading && (
        <div className="py-10 text-center">
          Loading payment details...
        </div>
      )}

      {!loading && payment && (
        <div className="space-y-6">

          {/* Amount */}

          <div className="rounded-xl bg-[var(--color-surface-secondary)] py-5 text-center">

            <p className="text-sm text-[var(--color-text-secondary)]">
              Amount to Pay
            </p>

            <h2 className="mt-2 text-4xl font-bold tracking-tight">
              ₹{payment.amount}
            </h2>

          </div>

          {/* QR */}

          <div className="flex justify-center">

            <div className="rounded-2xl border border-[var(--color-border)] bg-white p-4 shadow-sm">

              <QRCode
                value={payment.upiUrl}
                size={185}
              />

            </div>

          </div>

          {/* Receiver */}



          {/* Pay Button */}

          <Button
            fullWidth
            onClick={() =>
              window.open(
                payment.upiUrl,
                "_self"
              )
            }
          >
            <ExternalLink size={18} />
            Open UPI App
          </Button>

          {error && (
            <p className="text-center text-sm text-red-500">
              {error}
            </p>
          )}

        </div>
      )}
    </Modal>
  );
}
import {
  Button,
  Modal,
} from "@/components/common/ui";

import Bill from "@/features/billing/components/Bill";
import DownloadBillButton from "@/features/billing/components/DownloadBillButton";

import type { BillDetailResponse } from "../types";

interface BillDetailsDialogProps {
  open: boolean;
  bill: BillDetailResponse | null;
  loading: boolean;
  error?: string | null;
  onRetry?: () => void;
  onClose: () => void;
}

export default function BillDetailsDialog({
  open,
  bill,
  loading,
  error,
  onRetry,
  onClose,
}: BillDetailsDialogProps) {
  return (
    <Modal
      open={open}
      title={
        bill && !loading && !error
          ? `Bill #${bill.billId}`
          : "Bill Details"
      }
      size="lg"
      onClose={onClose}
      footer={
        <>
          {!loading && !error && bill && (
            <DownloadBillButton
              key={bill.billId}
              bill={bill}
            />
          )}

          <Button
            variant="secondary"
            onClick={onClose}
          >
            Close
          </Button>
        </>
      }
    >
      {loading ? (
        <div
          role="status"
          className="py-12 text-center text-sm text-[var(--color-text-secondary)]"
        >
          Loading bill...
        </div>
      ) : error ? (
        <div className="space-y-4 py-8 text-center">
          <p
            role="alert"
            className="text-sm text-red-500"
          >
            {error}
          </p>

          {onRetry && (
            <Button
              variant="secondary"
              onClick={onRetry}
            >
              Retry
            </Button>
          )}
        </div>
      ) : bill ? (
        <Bill bill={bill} />
      ) : (
        <p className="py-12 text-center text-sm text-[var(--color-text-secondary)]">
          Bill details are unavailable.
        </p>
      )}
    </Modal>
  );
}
import {
  Button,
  Modal,
} from "@/components/common/ui";

import Bill from "@/features/billing/components/Bill";
import DownloadBillButton from "@/features/billing/components/DownloadBillButton";

import { useBillDetails } from "../hooks";

import { getBillReference } from "@/features/billing/utils/getBillReference";

interface BillDetailsModalProps {
  billId: number | null;
  open: boolean;
  onClose: () => void;
}

export default function BillDetailsModal({
  billId,
  open,
  onClose,
}: BillDetailsModalProps) {
  if (!open || billId === null) {
    return null;
  }

  return (
    <BillDetailsContent
      key={billId}
      billId={billId}
      onClose={onClose}
    />
  );
}

interface BillDetailsContentProps {
  billId: number;
  onClose: () => void;
}

function BillDetailsContent({
  billId,
  onClose,
}: BillDetailsContentProps) {
  const {
    billDetail,
    loading,
    error,
    fetchBillDetails,
  } = useBillDetails(billId);

  return (
    <Modal
      open
      title={
        billDetail
        ? `Bill ${getBillReference(billDetail)}`
        : "Bill Details"
      }
      size="lg"
      onClose={onClose}
      footer={
        <>
          {!loading && !error && billDetail && (
            <DownloadBillButton bill={billDetail} />
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

          <Button
            variant="secondary"
            onClick={() => void fetchBillDetails()}
          >
            Retry
          </Button>
        </div>
      ) : billDetail ? (
        <Bill bill={billDetail} />
      ) : (
        <p className="py-12 text-center text-sm text-[var(--color-text-secondary)]">
          Bill details are unavailable.
        </p>
      )}
    </Modal>
  );
}
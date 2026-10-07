import { useRef, useState } from "react";

import { Download } from "lucide-react";
import { toast } from "sonner";

import Button from "@/components/common/ui/Button/Button";

import type { BillDocumentData } from "../types";

interface DownloadBillButtonProps {
  bill: BillDocumentData;
}

export default function DownloadBillButton({
  bill,
}: DownloadBillButtonProps) {
  const [downloading, setDownloading] = useState(false);
  const busyRef = useRef(false);

  async function handleDownload() {
    if (busyRef.current) {
      return;
    }

    busyRef.current = true;
    setDownloading(true);

    try {
      const { downloadBill } = await import(
        "../utils/downloadBill"
      );

      await downloadBill(bill);
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : "Unable to download the bill PDF."
      );
    } finally {
      busyRef.current = false;
      setDownloading(false);
    }
  }

  return (
    <Button
      variant="secondary"
      disabled={downloading}
      onClick={() => void handleDownload()}
    >
      <Download size={16} />

      {downloading
        ? "Preparing PDF..."
        : "Download PDF"}
    </Button>
  );
}
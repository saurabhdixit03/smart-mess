import { useEffect, useRef, useState } from "react";
import QRCode from "react-qr-code";
import { toast } from "sonner";

import {
  Check,
  Copy,
  Download,
  Printer,
} from "lucide-react";

import Button from "@/components/common/ui/Button/Button";
import Modal from "@/components/common/ui/Modal/Modal";

import { getCurrentOwnerMessId } from "@/features/auth/utils/auth.utils";

import { messRegistrationApi } from "../api/messRegistration.api";

import type { MessRegistrationLinkResponse } from "../types/messRegistration.types";

interface CustomerRegistrationLinkModalProps {
  open: boolean;
  onClose: () => void;
}

const SVG_NAMESPACE =
  "http://www.w3.org/2000/svg";

function validateRegistrationLink(
  registration: MessRegistrationLinkResponse,
  currentMessId: number
): void {
  if (
    registration.messId !== currentMessId
    || !registration.messName?.trim()
    || !registration.registrationCode?.trim()
    || !registration.registrationUrl?.trim()
  ) {
    throw new Error(
      "Unable to load a valid registration link for your mess."
    );
  }

  const url = new URL(
    registration.registrationUrl
  );

  if (
    !["http:", "https:"].includes(url.protocol)
    || url.username
    || url.password
    || url.searchParams.get("registrationCode")
      !== registration.registrationCode
  ) {
    throw new Error(
      "The registration link is invalid."
    );
  }
}

/*
 * Wraps text to fit the poster.
 *
 * Long words and URLs are split when necessary.
 */
function wrapText(
  context: CanvasRenderingContext2D,
  text: string,
  maxWidth: number
): string[] {
  const lines: string[] = [];
  let currentLine = "";

  for (const character of text) {
    if (character === "\n") {
      lines.push(currentLine.trim());
      currentLine = "";
      continue;
    }

    const candidate = currentLine + character;

    if (
      currentLine
      && context.measureText(candidate).width > maxWidth
    ) {
      const spaceIndex = currentLine.lastIndexOf(" ");

      if (spaceIndex > 0 && character !== " ") {
        lines.push(
          currentLine.slice(0, spaceIndex).trim()
        );

        currentLine =
          currentLine.slice(spaceIndex + 1)
          + character;
      } else {
        lines.push(currentLine.trim());
        currentLine = character.trimStart();
      }
    } else {
      currentLine = candidate;
    }
  }

  if (currentLine.trim()) {
    lines.push(currentLine.trim());
  }

  return lines;
}

function loadSvgImage(
  svg: SVGSVGElement
): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const svgContent =
      new XMLSerializer().serializeToString(svg);

    const blob = new Blob(
      [svgContent],
      { type: "image/svg+xml;charset=utf-8" }
    );

    const objectUrl =
      URL.createObjectURL(blob);

    const image = new Image();

    image.onload = () => {
      URL.revokeObjectURL(objectUrl);
      resolve(image);
    };

    image.onerror = () => {
      URL.revokeObjectURL(objectUrl);

      reject(
        new Error("Unable to prepare the QR image.")
      );
    };

    image.src = objectUrl;
  });
}

export default function CustomerRegistrationLinkModal({
  open,
  onClose,
}: CustomerRegistrationLinkModalProps) {
  const [registration, setRegistration] =
    useState<MessRegistrationLinkResponse | null>(null);

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const [retryCount, setRetryCount] =
    useState(0);

  const [copied, setCopied] =
    useState(false);

  const [exporting, setExporting] =
    useState<"PDF" | "PRINT" | null>(null);

  const qrContainerRef =
    useRef<HTMLDivElement | null>(null);

  const copyTimerRef =
    useRef<ReturnType<typeof setTimeout> | null>(null);

  /*
   * Prevent simultaneous export operations before React
   * has rendered the disabled buttons.
   */
  const exportBusyRef = useRef(false);

  useEffect(() => {
    if (!open) {
      return;
    }

    let disposed = false;

    setRegistration(null);
    setError(null);
    setLoading(true);
    setCopied(false);

    async function loadRegistrationLink() {
      try {
        const messId =
          getCurrentOwnerMessId();

        if (messId === null) {
          throw new Error(
            "Please sign out and sign in again to load your mess registration link."
          );
        }

        const response =
          await messRegistrationApi.getRegistrationLink();

        validateRegistrationLink(
          response.data,
          messId
        );

        if (!disposed) {
          setRegistration(response.data);
        }
      } catch (error) {
        if (!disposed) {
          setError(
            error instanceof Error
              ? error.message
              : "Unable to load the registration link."
          );
        }
      } finally {
        if (!disposed) {
          setLoading(false);
        }
      }
    }

    void loadRegistrationLink();

    return () => {
      disposed = true;
    };
  }, [open, retryCount]);

  useEffect(() => {
    return () => {
      if (copyTimerRef.current !== null) {
        clearTimeout(copyTimerRef.current);
      }
    };
  }, []);

  async function handleCopy() {
    if (!registration) {
      return;
    }

    try {
      await navigator.clipboard.writeText(
        registration.registrationUrl
      );

      setCopied(true);

      if (copyTimerRef.current !== null) {
        clearTimeout(copyTimerRef.current);
      }

      copyTimerRef.current = setTimeout(() => {
        setCopied(false);
        copyTimerRef.current = null;
      }, 2000);

      toast.success(
        "Registration link copied."
      );
    } catch {
      toast.error(
        "Unable to copy the link. You can select and copy it manually."
      );
    }
  }

  /*
   * Retains a white quiet zone around the QR code.
   */
  function createExportSvg(): SVGSVGElement {
    const sourceSvg =
      qrContainerRef.current?.querySelector("svg");

    if (!sourceSvg) {
      throw new Error(
        "The QR code is not ready yet."
      );
    }

    const exportSvg =
      document.createElementNS(
        SVG_NAMESPACE,
        "svg"
      );

    exportSvg.setAttribute(
      "xmlns",
      SVG_NAMESPACE
    );

    exportSvg.setAttribute(
      "viewBox",
      "0 0 240 240"
    );

    exportSvg.setAttribute("width", "240");
    exportSvg.setAttribute("height", "240");

    const background =
      document.createElementNS(
        SVG_NAMESPACE,
        "rect"
      );

    background.setAttribute("width", "240");
    background.setAttribute("height", "240");
    background.setAttribute("fill", "#ffffff");

    const qrSvg =
      sourceSvg.cloneNode(true) as SVGSVGElement;

    qrSvg.setAttribute("x", "20");
    qrSvg.setAttribute("y", "20");
    qrSvg.setAttribute("width", "200");
    qrSvg.setAttribute("height", "200");

    exportSvg.append(background, qrSvg);

    return exportSvg;
  }

  /*
   * Creates one shared A4 poster for PDF download and print.
   *
   * Browser-rendered text supports the same characters
   * displayed by the application, including local languages.
   */
  async function createPosterCanvas(
    details: MessRegistrationLinkResponse
  ): Promise<HTMLCanvasElement> {
    const qrImage =
      await loadSvgImage(createExportSvg());

    await document.fonts.ready;

    const canvas =
      document.createElement("canvas");

    canvas.width = 1240;
    canvas.height = 1754;

    const context =
      canvas.getContext("2d");

    if (!context) {
      throw new Error(
        "Unable to prepare the registration poster."
      );
    }

    context.fillStyle = "#ffffff";
    context.fillRect(
      0,
      0,
      canvas.width,
      canvas.height
    );

    context.strokeStyle = "#d1d5db";
    context.lineWidth = 2;

    context.strokeRect(
      60,
      60,
      canvas.width - 120,
      canvas.height - 120
    );

    context.textAlign = "center";
    context.textBaseline = "top";

    context.fillStyle = "#475569";
    context.font = "bold 23px Arial, sans-serif";

    context.fillText(
      "SMART MESS",
      canvas.width / 2,
      120
    );

    /*
     * Shrink long mess names until they fit in three lines.
     */
    let nameFontSize = 54;
    let nameLines: string[] = [];

    do {
      context.font =
        `bold ${nameFontSize}px Arial, sans-serif`;

      nameLines = wrapText(
        context,
        details.messName,
        980
      );

      if (nameLines.length <= 3 || nameFontSize <= 18) {
        break;
      }

      nameFontSize -= 2;
    } while (true);

    if (nameLines.length > 3) {
      throw new Error(
        "The mess name is too long for the registration poster."
      );
    }

    context.fillStyle = "#111827";

    let positionY = 185;

    for (const line of nameLines) {
      context.fillText(
        line,
        canvas.width / 2,
        positionY
      );

      positionY += nameFontSize + 12;
    }

    positionY += 8;

    context.font = "30px Arial, sans-serif";
    context.fillStyle = "#475569";

    context.fillText(
      "Customer Registration",
      canvas.width / 2,
      positionY
    );

    positionY += 65;

    const qrSize = 500;

    context.imageSmoothingEnabled = false;

    context.drawImage(
      qrImage,
      (canvas.width - qrSize) / 2,
      positionY,
      qrSize,
      qrSize
    );

    positionY += qrSize + 24;

    context.font = "bold 36px Arial, sans-serif";
    context.fillStyle = "#111827";

    context.fillText(
      "Scan to join our mess",
      canvas.width / 2,
      positionY
    );

    positionY += 75;

    const instructions = [
      "1. Scan the QR code using your phone camera.",
      "2. Enter your details and submit your registration.",
      "3. After owner approval, use the sign-in link sent to your email.",
    ];

    context.font = "27px Arial, sans-serif";

    const instructionLines = instructions.map(
      (instruction) =>
        wrapText(context, instruction, 890)
    );

    const instructionHeight =
      instructionLines.reduce(
        (height, lines) =>
          height + lines.length * 39 + 18,
        0
      ) + 42;

    context.fillStyle = "#f8fafc";

    context.fillRect(
      120,
      positionY,
      1000,
      instructionHeight
    );

    context.strokeStyle = "#e2e8f0";

    context.strokeRect(
      120,
      positionY,
      1000,
      instructionHeight
    );

    context.textAlign = "left";
    context.fillStyle = "#334155";

    let instructionY = positionY + 30;

    for (const lines of instructionLines) {
      for (const line of lines) {
        context.fillText(
          line,
          165,
          instructionY
        );

        instructionY += 39;
      }

      instructionY += 18;
    }

    positionY += instructionHeight + 40;

    context.textAlign = "center";
    context.font = "bold 25px Arial, sans-serif";
    context.fillStyle = "#111827";

    context.fillText(
      "Owner approval is required before you can sign in.",
      canvas.width / 2,
      positionY
    );

    positionY += 45;

    context.font = "23px Arial, sans-serif";
    context.fillStyle = "#475569";

    const noteLines = wrapText(
      context,
      "Register with an email address you can access. Contact the mess owner if you need help.",
      960
    );

    for (const line of noteLines) {
      context.fillText(
        line,
        canvas.width / 2,
        positionY
      );

      positionY += 33;
    }

    positionY += 38;

    context.font = "bold 19px Arial, sans-serif";
    context.fillStyle = "#64748b";

    context.fillText(
      "REGISTRATION LINK",
      canvas.width / 2,
      positionY
    );

    positionY += 34;

    context.font = "18px Arial, sans-serif";

    const linkLines = wrapText(
      context,
      details.registrationUrl,
      970
    );

    if (positionY + linkLines.length * 27 > canvas.height - 90) {
      throw new Error(
        "The registration link is too long for the A4 poster."
      );
    }

    for (const line of linkLines) {
      context.fillText(
        line,
        canvas.width / 2,
        positionY
      );

      positionY += 27;
    }

    return canvas;
  }

  async function handleDownloadPdf() {
    if (!registration || exportBusyRef.current) {
      return;
    }

    exportBusyRef.current = true;
    setExporting("PDF");

    try {
      const details = registration;

      const [canvas, { jsPDF }] =
        await Promise.all([
          createPosterCanvas(details),
          import("jspdf"),
        ]);

      const pdf = new jsPDF({
        orientation: "portrait",
        unit: "mm",
        format: "a4",
        compress: true,
      });

      pdf.setProperties({
        title:
          `${details.messName} - Customer Registration`,
        subject: "Customer registration QR poster",
        creator: "Smart Mess",
      });

      pdf.addImage(
        canvas.toDataURL("image/png"),
        "PNG",
        0,
        0,
        210,
        297
      );

      pdf.save(
        `smart-mess-${details.messId}-registration-qr.pdf`
      );

      toast.success(
        "Registration PDF downloaded."
      );
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : "Unable to download the registration PDF."
      );
    } finally {
      exportBusyRef.current = false;
      setExporting(null);
    }
  }

  async function handlePrint() {
    if (!registration || exportBusyRef.current) {
      return;
    }

    /*
     * Open synchronously during the button click so that
     * browsers can recognise this as a requested pop-up.
     */
    const printWindow = window.open(
      "",
      "_blank",
      "width=850,height=1000"
    );

    if (!printWindow) {
      toast.error(
        "Please allow pop-ups to print the QR poster."
      );

      return;
    }

    exportBusyRef.current = true;
    setExporting("PRINT");

    try {
      const details = registration;
      const printDocument = printWindow.document;

      printDocument.title =
        `${details.messName} - Customer Registration`;

      printDocument.body.textContent =
        "Preparing your registration poster...";

      const canvas =
        await createPosterCanvas(details);

      if (printWindow.closed) {
        return;
      }

      const style =
        printDocument.createElement("style");

      style.textContent = `
        @page {
          size: A4 portrait;
          margin: 0;
        }

        * {
          box-sizing: border-box;
        }

        html,
        body {
          margin: 0;
          padding: 0;
          background: #ffffff;
        }

        .poster {
          display: block;
          width: 210mm;
          height: 297mm;
          margin: 0 auto;
        }

        @media screen {
          body {
            background: #f3f4f6;
          }

          .poster {
            max-width: 100%;
            height: auto;
          }
        }
      `;

      printDocument.head.appendChild(style);
      printDocument.body.replaceChildren();

      const image =
        printDocument.createElement("img");

      image.className = "poster";

      image.alt =
        `${details.messName} customer registration poster`;

      printWindow.onafterprint = () => {
        printWindow.close();
      };

      image.onload = () => {
        setTimeout(() => {
          if (!printWindow.closed) {
            printWindow.focus();
            printWindow.print();
          }
        }, 100);
      };

      image.onerror = () => {
        printWindow.close();

        toast.error(
          "Unable to load the registration poster for printing."
        );
      };

      image.src =
        canvas.toDataURL("image/png");

      printDocument.body.appendChild(image);
    } catch (error) {
      printWindow.close();

      toast.error(
        error instanceof Error
          ? error.message
          : "Unable to prepare the QR poster."
      );
    } finally {
      exportBusyRef.current = false;
      setExporting(null);
    }
  }

  return (
    <Modal
      open={open}
      title="Customer Registration"
      onClose={onClose}
      footer={
        <Button
          variant="secondary"
          onClick={onClose}
          disabled={exporting !== null}
        >
          Close
        </Button>
      }
    >
      {loading && (
        <p className="py-8 text-center text-sm text-[var(--color-text-secondary)]">
          Loading your registration QR...
        </p>
      )}

      {!loading && error && (
        <div className="space-y-4 py-4 text-center">
          <p
            role="alert"
            className="text-sm text-red-500"
          >
            {error}
          </p>

          <Button
            variant="outline"
            onClick={() =>
              setRetryCount((count) => count + 1)
            }
          >
            Try Again
          </Button>
        </div>
      )}

      {!loading && !error && registration && (
        <div className="space-y-5">
          <div className="text-center">
            <h2 className="text-lg font-semibold text-[var(--color-text)]">
              {registration.messName}
            </h2>

            <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
              Share this QR or link so customers can
              submit a registration for your mess.
            </p>
          </div>

          <div className="flex justify-center">
            <div
              ref={qrContainerRef}
              className="rounded-xl border border-gray-200 bg-white p-5"
            >
              <QRCode
                value={registration.registrationUrl}
                size={200}
                level="M"
                bgColor="#ffffff"
                fgColor="#000000"
              />
            </div>
          </div>

          <div className="rounded-xl border border-amber-200 bg-amber-50 p-3">
            <p className="text-sm text-amber-900">
              Customers need your approval before
              they can sign in. Review their requests
              on the Customers page.
            </p>
          </div>

          <div className="space-y-2">
            <label
              htmlFor="mess-registration-link"
              className="text-sm font-medium text-[var(--color-text)]"
            >
              Registration Link
            </label>

            <input
              id="mess-registration-link"
              type="text"
              readOnly
              value={registration.registrationUrl}
              onFocus={(event) =>
                event.currentTarget.select()
              }
              className="w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-background-secondary)] px-3 py-2 text-sm text-[var(--color-text)] outline-none"
            />
          </div>

          <div className="flex flex-wrap justify-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                void handleCopy();
              }}
            >
              {copied
                ? <Check size={16} />
                : <Copy size={16} />}

              {copied ? "Copied" : "Copy Link"}
            </Button>

            <Button
              variant="outline"
              size="sm"
              disabled={exporting !== null}
              onClick={() => {
                void handleDownloadPdf();
              }}
            >
              <Download size={16} />

              {exporting === "PDF"
                ? "Preparing PDF..."
                : "Download PDF"}
            </Button>

            <Button
              variant="outline"
              size="sm"
              disabled={exporting !== null}
              onClick={() => {
                void handlePrint();
              }}
            >
              <Printer size={16} />

              {exporting === "PRINT"
                ? "Preparing..."
                : "Print QR"}
            </Button>
          </div>

          <p className="text-center text-xs text-[var(--color-text-secondary)]">
            Download or print an A4 poster with your
            mess name, registration QR, and joining
            instructions.
          </p>
        </div>
      )}
    </Modal>
  );
}
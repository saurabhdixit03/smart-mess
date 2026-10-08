import type { BillDocumentData } from "../types";
import { getBillReference } from "./getBillReference";

type Charge = {
  description: string;
  quantity: number;
  rate: number;
};

const currencyFormat = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
});

const MONTHS = [
  "January", "February", "March", "April",
  "May", "June", "July", "August",
  "September", "October", "November", "December",
];

function getCanvasContext(
  canvas: HTMLCanvasElement
): CanvasRenderingContext2D {
  const context = canvas.getContext("2d");

  if (!context) {
    throw new Error("Unable to prepare the bill PDF.");
  }

  return context;
}

function formatDate(value: string): string {
  const date = new Date(value);

  return Number.isNaN(date.getTime())
    ? "—"
    : date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      });
}

function getCharges(bill: BillDocumentData): Charge[] {
  const charges = new Map<string, Charge>();

  function add(
    description: string,
    quantity: number,
    rate: number
  ) {
    const key = `${description}:${rate}`;
    const existing = charges.get(key);

    if (existing) {
      existing.quantity += quantity;
    } else {
      charges.set(key, { description, quantity, rate });
    }
  }

  for (const meal of bill.mealRecords) {
    add(
      meal.mealOption === "FULL" ? "Full Meal" : "Half Meal",
      1,
      meal.mealPrice
    );

    if (meal.extraRotiCount > 0) {
      add(
        "Extra Roti",
        meal.extraRotiCount,
        meal.extraRotiPrice
      );
    }
  }

  const order: Record<string, number> = {
    "Full Meal": 0,
    "Half Meal": 1,
    "Extra Roti": 2,
  };

  return [...charges.values()].sort(
    (first, second) =>
      order[first.description] - order[second.description] ||
      first.rate - second.rate
  );
}

export async function downloadBill(
  bill: BillDocumentData
): Promise<void> {
  const { jsPDF } = await import("jspdf");

  await document.fonts.ready;

const reference = getBillReference(bill);

  const payment = bill.payment;
  const sandbox = payment?.environment === "SANDBOX";
  const paid = bill.billStatus === "PAID";

  const title = sandbox
    ? "BILL & TEST PAYMENT RECORD"
    : paid && payment
      ? "PAID BILL & RECEIPT"
      : "BILL";

  const width = 1240;
  const pageHeight = 1754;
  const margin = 90;
  const contentWidth = width - margin * 2;

  const charges = getCharges(bill);
  const workingCanvas = document.createElement("canvas");

  workingCanvas.width = width;
  workingCanvas.height = 2400 + charges.length * 70;

  const context = getCanvasContext(workingCanvas);

  context.fillStyle = "#ffffff";
  context.fillRect(
    0,
    0,
    workingCanvas.width,
    workingCanvas.height
  );
  context.textBaseline = "top";

  let y = margin;

  function text(
    value: string,
    size = 23,
    bold = false,
    color = "#334155"
  ) {
    context.font =
      `${bold ? "bold " : ""}${size}px Arial, sans-serif`;

    context.fillStyle = color;
    context.textAlign = "left";

    const lines: string[] = [];
    let line = "";

    for (const character of value) {
      if (
        character === "\n" ||
        (
          line &&
          context.measureText(line + character).width > contentWidth
        )
      ) {
        lines.push(line);
        line = character === "\n" ? "" : character;
      } else {
        line += character;
      }
    }

    if (line) {
      lines.push(line);
    }

    for (const item of lines) {
      context.fillText(item, margin, y);
      y += size + 9;
    }
  }

  function rule() {
    context.strokeStyle = "#d1d5db";
    context.lineWidth = 2;
    context.beginPath();
    context.moveTo(margin, y);
    context.lineTo(width - margin, y);
    context.stroke();
    y += 18;
  }

  function amountRow(
    label: string,
    amount: number,
    bold = false
  ) {
    context.font =
      `${bold ? "bold " : ""}25px Arial, sans-serif`;

    context.fillStyle = "#111827";
    context.textAlign = "left";
    context.fillText(label, margin, y);

    context.textAlign = "right";
    context.fillText(
      currencyFormat.format(amount),
      width - margin,
      y
    );

    context.textAlign = "left";
    y += 40;
  }

  text("SMART MESS", 18, true, "#64748b");
  y += 4;
  text(bill.messName, 34, true, "#111827");
  text(title, 21, true);
  y += 12;
  rule();

  text(reference, 26, true, "#111827");
  text(`Issued: ${formatDate(bill.generatedAt)}`, 22);
  text(
    `Billing period: ${MONTHS[bill.billingMonth - 1]} ${bill.billingYear}`,
    22
  );
  text(
    `Status: ${
      sandbox ? "Test payment recorded" : paid ? "Paid" : "Unpaid"
    }`,
    22
  );

  if (sandbox) {
    y += 6;
    text(
      "SANDBOX TEST PAYMENT — No real money was charged.",
      21,
      true,
      "#92400e"
    );
  }

  y += 16;
  text("BILLED TO", 18, true, "#64748b");
  text(bill.customerName, 25, true, "#111827");
  text(`Customer #${bill.customerId}`, 21);
  text(bill.customerMobileNumber, 21);
  text(bill.customerEmail, 21);

  y += 18;
  rule();
  text("CHARGES", 21, true);
  text(`${bill.mealRecordCount} collected meals`, 21);
  y += 10;

  const quantityX = 650;
  const rateX = 850;
  const amountX = width - margin;

  function tableCell(
    value: string,
    x: number,
    align: CanvasTextAlign,
    bold = false
  ) {
    context.font =
      `${bold ? "bold " : ""}22px Arial, sans-serif`;
    context.fillStyle = "#334155";
    context.textAlign = align;
    context.fillText(value, x, y);
  }

  tableCell("Description", margin, "left", true);
  tableCell("Qty", quantityX, "right", true);
  tableCell("Rate", rateX, "right", true);
  tableCell("Amount", amountX, "right", true);
  y += 36;
  rule();

  for (const charge of charges) {
    tableCell(charge.description, margin, "left");
    tableCell(String(charge.quantity), quantityX, "right");
    tableCell(currencyFormat.format(charge.rate), rateX, "right");
    tableCell(
      currencyFormat.format(charge.quantity * charge.rate),
      amountX,
      "right"
    );
    y += 42;
  }

  context.textAlign = "left";
  y += 12;
  rule();
  y += 6;

  const recordedPayment = payment?.paymentAmount ?? 0;

  amountRow("Bill Total", bill.totalAmount, true);
  amountRow(
    sandbox ? "Test Payment" : "Recorded Payment",
    recordedPayment
  );
  amountRow(
    sandbox ? "Test Balance" : "Amount Due",
    Math.max(0, bill.totalAmount - recordedPayment),
    true
  );

  y += 18;

  if (payment) {
    rule();
    text(
      sandbox ? "TEST PAYMENT RECORD" : "PAYMENT RECEIPT",
      21,
      true
    );
    text(`Receipt reference: #${payment.paymentId}`, 21);
    text(`Payment date: ${formatDate(payment.paidAt)}`, 21);
    text("Provider: Cashfree", 21);
    text(
      `Amount: ${currencyFormat.format(payment.paymentAmount)}`,
      21
    );

    if (payment.gatewayPaymentId) {
      text(
        `Transaction reference: ${payment.gatewayPaymentId}`,
        20
      );
    }

    if (payment.gatewayOrderId) {
      text(`Order reference: ${payment.gatewayOrderId}`, 20);
    }

    if (payment.environment === null) {
      text(
        "Gateway environment metadata is unavailable for this historical payment.",
        19,
        false,
        "#64748b"
      );
    }
  } else if (paid) {
    text(
      "Marked paid, but the payment record is unavailable. No payment receipt is included.",
      21,
      false,
      "#b91c1c"
    );
  }

  y += 18;
  rule();
  text(
    "Charges use recorded meal prices. Only meals attached to this bill are included.",
    18,
    false,
    "#64748b"
  );
  text(
    "Customer and mess details reflect current account information.",
    18,
    false,
    "#64748b"
  );

  const contentHeight = y - margin + 20;
  const availableHeight = pageHeight - margin - 130;
  const scale = Math.min(1, availableHeight / contentHeight);

  const pageCanvas = document.createElement("canvas");
  pageCanvas.width = width;
  pageCanvas.height = pageHeight;

  const pageContext = getCanvasContext(pageCanvas);

  pageContext.fillStyle = "#ffffff";
  pageContext.fillRect(0, 0, width, pageHeight);

  const scaledWidth = contentWidth * scale;

  pageContext.drawImage(
    workingCanvas,
    margin,
    margin,
    contentWidth,
    contentHeight,
    (width - scaledWidth) / 2,
    margin,
    scaledWidth,
    contentHeight * scale
  );

  pageContext.font = "18px Arial, sans-serif";
  pageContext.fillStyle = "#64748b";
  pageContext.textAlign = "right";
  pageContext.fillText(
    `${reference} · Page 1 of 1`,
    width - margin,
    pageHeight - 65
  );

  const pdf = new jsPDF({
    orientation: "portrait",
    unit: "mm",
    format: "a4",
    compress: true,
  });

  pdf.setProperties({
    title: `${bill.messName} - ${reference}`,
    subject: title,
    creator: "Smart Mess",
  });

  pdf.addImage(
    pageCanvas.toDataURL("image/png"),
    "PNG",
    0,
    0,
    210,
    297
  );

  pdf.save(`${reference}${sandbox ? "-test" : ""}.pdf`);
}
import type {
  BillDocumentData,
  BillDocumentMeal,
} from "../types";

interface BillProps {
  bill: BillDocumentData;
}

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

function formatDate(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return date.toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function getBillReference(bill: BillDocumentData): string {
  const month = String(bill.billingMonth).padStart(2, "0");
  const id = String(bill.billId).padStart(6, "0");

  return `SM-${bill.billingYear}${month}-${id}`;
}

function getCharges(records: BillDocumentMeal[]): Charge[] {
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

  for (const record of records) {
    add(
      record.mealOption === "FULL" ? "Full Meal" : "Half Meal",
      1,
      record.mealPrice
    );

    if (record.extraRotiCount > 0) {
      add(
        "Extra Roti",
        record.extraRotiCount,
        record.extraRotiPrice
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

export default function Bill({ bill }: BillProps) {
  const reference = getBillReference(bill);
  const charges = getCharges(bill.mealRecords);

  const records = [...bill.mealRecords].sort(
    (first, second) =>
      first.collectedAt.localeCompare(second.collectedAt) ||
      first.mealRecordId - second.mealRecordId
  );

  const payment = bill.payment;
  const paid = bill.billStatus === "PAID";
  const sandbox = payment?.environment === "SANDBOX";

  return (
    <article className="min-w-0 space-y-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
      <header className="flex flex-wrap items-start justify-between gap-3 border-b border-[var(--color-border)] pb-3">
        <div className="min-w-0 flex-1">
          <p className="text-[10px] font-semibold uppercase tracking-widest text-[var(--color-text-secondary)]">
            Smart Mess
          </p>

          <h2 className="mt-1 break-words text-lg font-semibold">
            {bill.messName}
          </h2>

          <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
            {sandbox
              ? "Bill & Test Payment Record"
              : paid && payment
                ? "Paid Bill & Receipt"
                : "Bill"}
          </p>
        </div>

        <div className="min-w-0 text-right">
          <p className="break-all text-sm font-semibold">
            {reference}
          </p>

          <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
            Issued {formatDate(bill.generatedAt)}
          </p>

          <p
            className={`mt-1 text-xs font-semibold ${
              paid
                ? "text-[var(--color-success)]"
                : "text-amber-700"
            }`}
          >
            {sandbox ? "Test payment" : paid ? "Paid" : "Unpaid"}
          </p>
        </div>
      </header>

      {sandbox && (
        <p className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-900">
          Sandbox test payment. No real money was charged.
        </p>
      )}

      <div className="grid grid-cols-2 gap-3 text-xs">
        <section className="min-w-0">
          <h3 className="text-[10px] font-semibold uppercase tracking-wide text-[var(--color-text-secondary)]">
            Billed To
          </h3>

          <p className="mt-1 break-words text-sm font-semibold">
            {bill.customerName}
          </p>

          <p className="mt-1 text-[var(--color-text-secondary)]">
            Customer #{bill.customerId}
          </p>

          <p className="mt-1">{bill.customerMobileNumber}</p>
          <p className="mt-1 break-all">{bill.customerEmail}</p>
        </section>

        <section className="min-w-0 text-right">
          <h3 className="text-[10px] font-semibold uppercase tracking-wide text-[var(--color-text-secondary)]">
            Billing Period
          </h3>

          <p className="mt-1 text-sm font-semibold">
            {MONTHS[bill.billingMonth - 1]} {bill.billingYear}
          </p>

          <p className="mt-1 text-[var(--color-text-secondary)]">
            {bill.mealRecordCount} collected meals
          </p>

          {records.length > 0 && (
            <p className="mt-1 text-[var(--color-text-secondary)]">
              {formatDate(records[0].collectedAt)}
              {" – "}
              {formatDate(records[records.length - 1].collectedAt)}
            </p>
          )}
        </section>
      </div>

      <section className="min-w-0">
        <h3 className="mb-2 text-xs font-semibold">Charges</h3>

        <table className="w-full table-fixed text-xs">
          <thead>
            <tr className="border-y border-[var(--color-border)] text-[var(--color-text-secondary)]">
              <th className="w-[34%] py-2 pr-2 text-left font-medium">
                Description
              </th>
              <th className="w-[10%] py-2 text-right font-medium">
                Qty
              </th>
              <th className="w-[27%] px-1 py-2 text-right font-medium">
                Rate
              </th>
              <th className="w-[29%] py-2 pl-1 text-right font-medium">
                Amount
              </th>
            </tr>
          </thead>

          <tbody>
            {charges.map((charge) => (
              <tr
                key={`${charge.description}:${charge.rate}`}
                className="border-b border-[var(--color-border)]"
              >
                <td className="break-words py-2 pr-2">
                  {charge.description}
                </td>
                <td className="py-2 text-right">
                  {charge.quantity}
                </td>
                <td className="break-words px-1 py-2 text-right tabular-nums">
                  {currencyFormat.format(charge.rate)}
                </td>
                <td className="break-words py-2 pl-1 text-right tabular-nums">
                  {currencyFormat.format(charge.quantity * charge.rate)}
                </td>
              </tr>
            ))}
          </tbody>

          <tfoot>
            <tr>
              <th
                colSpan={3}
                className="pb-2 pt-4 text-left text-sm font-semibold"
              >
                Bill Total
              </th>
              <td className="break-words pb-2 pl-1 pt-4 text-right text-sm font-semibold tabular-nums">
                {currencyFormat.format(bill.totalAmount)}
              </td>
            </tr>
          </tfoot>
        </table>
      </section>

      {payment && (
        <section className="border-t border-[var(--color-border)] pt-3">
          <h3 className="mb-2 text-xs font-semibold">
            {sandbox ? "Test Payment Record" : "Payment Receipt"}
          </h3>

          <dl className="grid grid-cols-2 gap-x-3 gap-y-2 text-xs">
            <div>
              <dt className="text-[var(--color-text-secondary)]">
                Receipt
              </dt>
              <dd className="mt-0.5 font-medium">
                #{payment.paymentId}
              </dd>
            </div>

            <div className="text-right">
              <dt className="text-[var(--color-text-secondary)]">
                Paid On
              </dt>
              <dd className="mt-0.5">
                {formatDate(payment.paidAt)}
              </dd>
            </div>

            <div>
              <dt className="text-[var(--color-text-secondary)]">
                Provider
              </dt>
              <dd className="mt-0.5">Cashfree</dd>
            </div>

            <div className="text-right">
              <dt className="text-[var(--color-text-secondary)]">
                Amount
              </dt>
              <dd className="mt-0.5 font-medium tabular-nums">
                {currencyFormat.format(payment.paymentAmount)}
              </dd>
            </div>

            {payment.gatewayPaymentId && (
              <div className="col-span-2 min-w-0">
                <dt className="text-[var(--color-text-secondary)]">
                  Transaction Reference
                </dt>
                <dd className="mt-0.5 break-all font-mono">
                  {payment.gatewayPaymentId}
                </dd>
              </div>
            )}
          </dl>

          {payment.environment === null && (
            <p className="mt-2 text-[11px] text-[var(--color-text-secondary)]">
              Gateway environment metadata is unavailable for
              this historical payment.
            </p>
          )}
        </section>
      )}

      {paid && !payment && (
        <p role="alert" className="text-xs text-red-500">
          Marked paid, but the payment record is unavailable.
          A payment receipt cannot be displayed.
        </p>
      )}

      <p className="border-t border-[var(--color-border)] pt-3 text-[10px] leading-4 text-[var(--color-text-secondary)]">
        Charges use recorded meal prices. Only meals attached
        to this bill are included. Customer and mess details
        reflect current account information.
      </p>

      <details className="min-w-0 border-t border-[var(--color-border)] pt-3">
        <summary className="cursor-pointer text-xs font-semibold">
          Detailed Meal Statement ({records.length})
        </summary>

        <div className="mt-3 overflow-x-auto">
          <table className="w-full text-xs">
            <thead>
              <tr className="border-b border-[var(--color-border)] text-[var(--color-text-secondary)]">
                <th className="py-2 pr-2 text-left font-medium">
                  Date
                </th>
                <th className="px-1 py-2 text-left font-medium">
                  Session
                </th>
                <th className="px-1 py-2 text-left font-medium">
                  Meal
                </th>
                <th className="px-1 py-2 text-right font-medium">
                  Meal Charge
                </th>
                <th className="px-1 py-2 text-right font-medium">
                  Extra Rotis
                </th>
                <th className="py-2 pl-1 text-right font-medium">
                  Total
                </th>
              </tr>
            </thead>

            <tbody>
              {records.map((record) => (
                <tr
                  key={record.mealRecordId}
                  className="border-b border-[var(--color-border)]"
                >
                  <td className="py-2 pr-2">
                    {formatDate(record.collectedAt)}
                  </td>
                  <td className="px-1 py-2">
                    {record.mealSession === "LUNCH" ? "Lunch" : "Dinner"}
                  </td>
                  <td className="px-1 py-2">
                    {record.mealOption === "FULL" ? "Full" : "Half"}
                  </td>
                  <td className="px-1 py-2 text-right tabular-nums">
                    {currencyFormat.format(record.mealPrice)}
                  </td>
                  <td className="px-1 py-2 text-right tabular-nums">
                    {record.extraRotiCount > 0
                      ? `${record.extraRotiCount} × ${currencyFormat.format(
                          record.extraRotiPrice
                        )}`
                      : "—"}
                  </td>
                  <td className="py-2 pl-1 text-right font-medium tabular-nums">
                    {currencyFormat.format(record.totalAmount)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </details>
    </article>
  );
}
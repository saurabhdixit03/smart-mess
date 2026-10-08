type BillReferenceData = {
  billId: number;
  billingMonth: number;
  billingYear: number;
};

export function getBillReference(
  bill: BillReferenceData
): string {
  const month = String(bill.billingMonth).padStart(2, "0");
  const id = String(bill.billId).padStart(6, "0");

  return `SM-${bill.billingYear}${month}-${id}`;
}
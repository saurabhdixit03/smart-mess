import { useState } from "react";
import PageHeader from "@/components/common/ui/PageHeader";



import {
  BillingSummary,
  BillingTable,
  GenerateBillCard,
  BillDetailsDialog,
} from "../components";

import {
  useBillingOverview,
  useGenerateBills,
  useBillDetails,
} from "../hooks";

export default function BillingPage() {

  const [billingMonth, setBillingMonth] =
    useState(new Date().getMonth() + 1);

  const [billingYear, setBillingYear] =
    useState(new Date().getFullYear());

  const [selectedBillId, setSelectedBillId] =
    useState<number | null>(null);

  const [dialogOpen, setDialogOpen] =
    useState(false);

  const {
    overview,
    refreshBillingOverview,
  } = useBillingOverview(
    billingMonth,
    billingYear
  );

  const {
    loading: generating,
    generateBills,
  } = useGenerateBills(
    refreshBillingOverview
  );

  const {
    bill,
    loading: billLoading,
  } = useBillDetails(selectedBillId);

  return (
    <>
      <div className="space-y-6">

        <PageHeader
          title="Billing"
          description="Generate and manage monthly customer bills."
        />

        <GenerateBillCard
          billingMonth={billingMonth}
          billingYear={billingYear}
          loading={generating}
          onMonthChange={setBillingMonth}
          onYearChange={setBillingYear}
          onGenerate={() =>
            generateBills(
              billingMonth,
              billingYear
            )
          }
        />

        {overview && (
          <>
            <BillingSummary
              summary={overview.summary}
            />

            <BillingTable
  bills={overview.bills}
  onViewBill={(billId: number) => {
    setSelectedBillId(billId);
    setDialogOpen(true);
  }}
/>
          </>
        )}

        <BillDetailsDialog
          open={dialogOpen}
          bill={bill}
          loading={billLoading}
          onClose={() => {
            setDialogOpen(false);
            setSelectedBillId(null);
          }}
        />

      </div>
    </>
  );
}
import { Search } from "lucide-react";

import {
  Input,
  PageHeader,
  SearchToolbar,
} from "@/components/common/ui";

import BillsList from "../components/BillsList";

import { useBills } from "../hooks";

export default function BillingPage() {

  // Temporary until authentication is added
  const customerId = 11;

  const {
    bills,
    loading,
    error,
  } = useBills(customerId);

  return (
    <div className="space-y-6">

      <PageHeader
        title="My Bills"
        description="View your monthly billing history."
      />

      <SearchToolbar>

        <SearchToolbar.Left>

          <Input
            placeholder="Search bills..."
            leftIcon={<Search size={18} />}
            className="max-w-sm"
          />

        </SearchToolbar.Left>

      </SearchToolbar>

      {loading && (
        <p>Loading bills...</p>
      )}

      {error && (
        <p className="text-red-500">
          {error}
        </p>
      )}

      {!loading && !error && (
        <BillsList bills={bills} />
      )}

    </div>
  );
}
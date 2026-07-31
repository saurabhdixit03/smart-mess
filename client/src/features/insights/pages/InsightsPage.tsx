import { useState } from "react";

import PageHeader from "@/components/common/ui/PageHeader";

import {
  CustomerInsights,
  FinancialInsights,
  InsightsFilters,
  MealInsights,
} from "../components";

import { useInsights } from "../hooks";

export default function InsightsPage() {

  const today = new Date();

  const [month, setMonth] =
    useState(today.getMonth() + 1);

  const [year, setYear] =
    useState(today.getFullYear());

  const {
    insights,
    loading,
    fetchInsights,
  } = useInsights(month, year);

  if (loading || !insights) {

    return (
      <div className="p-6">
        Loading insights...
      </div>
    );

  }

  return (

    <div className="space-y-6">

      <PageHeader
        title="Insights"
        description="Monthly business insights and analytics."
      />

      <InsightsFilters
        month={month}
        year={year}
        onMonthChange={setMonth}
        onYearChange={setYear}
        onSearch={() =>
          fetchInsights(month, year)
        }
      />

      <FinancialInsights
        financial={insights.financial}
      />

      <CustomerInsights
        customers={insights.customers}
      />

      <MealInsights
        meals={insights.meals}
      />

    </div>

  );

}
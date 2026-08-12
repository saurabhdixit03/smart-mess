import { useState } from "react";

import { useTodayMenus } from "../hooks/useTodayMenus";
import { useDashboard } from "../hooks/useDashboard";

import MealResponseSummary from "../components/MealResponseSummary/MealResponseSummary";

import MealSessionSelector from "@/components/common/business/MealSessionSelector/MealSessionSelector";

export default function DashboardPage() {

  const {
    todayMenus,
    loading: menusLoading,
    error: menusError,
  } = useTodayMenus();

  const [
    selectedSession,
    setSelectedSession,
  ] = useState<"LUNCH" | "DINNER">(
    "LUNCH"
  );

  const {
    dashboard,
    loading,
    error,
  } = useDashboard(selectedSession);

  if (menusLoading || loading) {

    return (

      <div className="py-12 text-center">

        Loading dashboard...

      </div>

    );

  }

  if (menusError || error) {

    return (

      <div className="py-12 text-center text-red-500">

        {menusError ?? error}

      </div>

    );

  }

  return (

    <section className="space-y-6">

      

      {/* Today's Menu */}

      <div className="space-y-3">

       

        <MealSessionSelector
          menus={todayMenus}
          selectedSession={selectedSession}
          onSelect={setSelectedSession}
        />

      </div>

      {/* Today's Overview */}

      {dashboard && (

        <div className="space-y-3">

         

          <MealResponseSummary
            dashboard={dashboard}
          />

        </div>

      )}

      {/* Future Dashboard Widgets */}

      {/*
        Collection Queue
        Customer Alerts
        Daily Analytics
        Reports
      */}

    </section>

  );

}
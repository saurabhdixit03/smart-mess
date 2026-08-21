import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { useTodayMenus } from "../hooks/useTodayMenus";
import { useDashboard } from "../hooks/useDashboard";

import MealResponseSummary from "../components/MealResponseSummary/MealResponseSummary";

import MealSessionSelector from "@/components/common/business/MealSessionSelector/MealSessionSelector";
import { Button, Card } from "@/components/common/ui";

import type { MealSession } from "../types/dashboard.types";

export default function DashboardPage() {
  const navigate = useNavigate();

  const {
    todayMenus,
    loading: menusLoading,
    error: menusError,
  } = useTodayMenus();

  const [selectedSession, setSelectedSession] =
    useState<MealSession>("LUNCH");

  /*
   * If the currently selected session has no menu,
   * automatically select the first available session.
   *
   * Example:
   * Lunch not published
   * Dinner published
   * → Dinner becomes selected.
   */
  useEffect(() => {
    if (todayMenus.length === 0) {
      return;
    }

    const selectedMenuExists = todayMenus.some(
      (menu) =>
        menu.mealSession === selectedSession
    );

    if (!selectedMenuExists) {
      setSelectedSession(
        todayMenus[0].mealSession
      );
    }
  }, [todayMenus, selectedSession]);

  const hasSelectedMenu = todayMenus.some(
    (menu) =>
      menu.mealSession === selectedSession
  );

  const {
    dashboard,
    loading,
    error,
  } = useDashboard(
    selectedSession,
    hasSelectedMenu && !menusLoading
  );

  if (menusLoading) {
    return (
      <div className="py-12 text-center">
        Loading dashboard...
      </div>
    );
  }

  /*
   * Failure while loading today's menus is a real
   * application/API failure.
   */
  if (menusError) {
    return (
      <div className="py-12 text-center text-red-500">
        {menusError}
      </div>
    );
  }

  /*
   * No menu is a valid operational state,
   * not an application error.
   */
  if (todayMenus.length === 0) {
    return (
      <section className="space-y-6">
        <Card>
          <Card.Body className="py-12 text-center">
            <h2 className="text-lg font-semibold text-[var(--color-text)]">
              No menu published for today
            </h2>

            <p className="mx-auto mt-2 max-w-md text-sm text-[var(--color-text-secondary)]">
              Publish today's menu to start collecting
              customer responses and view the live
              dashboard.
            </p>

            <Button
              type="button"
              className="mt-6"
              onClick={() =>
                navigate("/owner/menu")
              }
            >
              Go to Menu
            </Button>
          </Card.Body>
        </Card>
      </section>
    );
  }

  if (loading) {
    return (
      <div className="py-12 text-center">
        Loading dashboard...
      </div>
    );
  }

  /*
   * At this point a menu exists, so a dashboard
   * request failure represents an actual problem.
   */
  if (error) {
    return (
      <div className="py-12 text-center text-red-500">
        {error}
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
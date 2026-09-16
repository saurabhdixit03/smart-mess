import {
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  useNavigate,
} from "react-router-dom";

import {
  Button,
  Card,
  PageHeader,
} from "@/components/common/ui";

import MealResponseSummary from "../components/MealResponseSummary/MealResponseSummary";

import {
  useDashboard,
} from "../hooks/useDashboard";

import {
  useTodayMenus,
} from "../hooks/useTodayMenus";

import type {
  MealSession,
} from "../types/dashboard.types";

const MEAL_SESSIONS: MealSession[] = [
  "LUNCH",
  "DINNER",
];

export default function DashboardPage() {
  const navigate = useNavigate();

  const {
    todayMenus,
    loading: menusLoading,
    error: menusError,
  } = useTodayMenus();

  const [
    selectedSession,
    setSelectedSession,
  ] = useState<MealSession>(
    "LUNCH"
  );

  const availableSessions =
    useMemo(
      () =>
        new Set(
          todayMenus.map(
            (menu) =>
              menu.mealSession
          )
        ),
      [todayMenus]
    );

  const selectedMenu =
    useMemo(
      () =>
        todayMenus.find(
          (menu) =>
            menu.mealSession ===
            selectedSession
        ),
      [
        todayMenus,
        selectedSession,
      ]
    );

  useEffect(() => {
    if (todayMenus.length === 0) {
      return;
    }

    if (
      availableSessions.has(
        selectedSession
      )
    ) {
      return;
    }

    const fallbackSession =
      availableSessions.has("LUNCH")
        ? "LUNCH"
        : "DINNER";

    setSelectedSession(
      fallbackSession
    );
  }, [
    todayMenus,
    availableSessions,
    selectedSession,
  ]);

  const hasSelectedMenu =
    selectedMenu !== undefined;

  const {
    dashboard,
    loading,
    error,
  } = useDashboard(
    selectedSession,
    hasSelectedMenu &&
      !menusLoading
  );

  if (menusLoading) {
    return (
      <div className="py-12 text-center text-[var(--color-text-secondary)]">
        Loading dashboard...
      </div>
    );
  }

  if (menusError) {
    return (
      <div className="py-12 text-center text-[var(--color-danger)]">
        {menusError}
      </div>
    );
  }

  if (todayMenus.length === 0) {
    return (
      <section className="space-y-6">

        <PageHeader
          title="Dashboard"
          description="Monitor today's live mess operations."
        />

        <Card>

          <Card.Body className="py-12 text-center">

            <h2 className="text-lg font-semibold text-[var(--color-text)]">
              No menu published for today
            </h2>

            <p className="mx-auto mt-2 max-w-md text-sm text-[var(--color-text-secondary)]">
              Publish today's menu to start collecting customer responses and view the live dashboard.
            </p>

            <Button
              type="button"
              className="mt-6"
              onClick={() =>
                navigate(
                  "/owner/menu"
                )
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
      <div className="py-12 text-center text-[var(--color-text-secondary)]">
        Loading dashboard...
      </div>
    );
  }

  if (error) {
    return (
      <div className="py-12 text-center text-[var(--color-danger)]">
        {error}
      </div>
    );
  }

  return (
    <section className="space-y-5">

      <PageHeader
        title="Dashboard"
        description="Monitor today's live mess operations."
        action={
          <div
            className="
              inline-flex
              rounded-lg
              border
              border-[var(--color-border)]
              bg-[var(--color-surface)]
              p-1
            "
          >
            {MEAL_SESSIONS.map(
              (session) => {
                const available =
                  availableSessions.has(
                    session
                  );

                const selected =
                  session ===
                  selectedSession;

                return (
                  <button
                    key={session}
                    type="button"
                    disabled={
                      !available
                    }
                    onClick={() =>
                      setSelectedSession(
                        session
                      )
                    }
                    className={`
                      rounded-md
                      px-5
                      py-2
                      text-sm
                      font-medium
                      transition-all
                      duration-200
                      ${
                        selected
                          ? "bg-[var(--color-primary)] text-white shadow-sm"
                          : available
                            ? "text-[var(--color-text-secondary)] hover:bg-[var(--color-background)] hover:text-[var(--color-text)]"
                            : "cursor-not-allowed text-[var(--color-text-secondary)] opacity-40"
                      }
                    `}
                  >
                    {session ===
                    "LUNCH"
                      ? "Lunch"
                      : "Dinner"}
                  </button>
                );
              }
            )}
          </div>
        }
      />

      {dashboard &&
        selectedMenu && (
          <MealResponseSummary
            dashboard={
              dashboard
            }
            menu={selectedMenu}
          />
        )}

    </section>
  );
}
import { useEffect, useState } from "react";

import PageHeader from "@/components/common/ui/PageHeader";
import MealFeedbackModal from "@/features/customer/meal-feedback/components/MealFeedbackModal";
import { useMealFeedback } from "@/features/customer/meal-feedback/hooks/useMealFeedback";

import TodayMenus from "../components/TodayMenus";
import { MENU_PAGE } from "../constants";
import { useMenus } from "../hooks";

export default function MenuPage() {
  const {
    todayMenus,
    loading,
    error,
    fetchTodayMenus,
  } = useMenus();

  const {
    pendingFeedback,
    submitting,
    error: feedbackError,
    submitFeedback,
  } = useMealFeedback();

  const [feedbackOpen, setFeedbackOpen] =
    useState(false);

  const [feedbackSubmitted, setFeedbackSubmitted] =
    useState(false);

  useEffect(() => {
    if (pendingFeedback && !feedbackSubmitted) {
      setFeedbackOpen(true);
    }
  }, [
    pendingFeedback?.mealRecordId,
    feedbackSubmitted,
  ]);

  async function handleSubmitFeedback(
    payload: Parameters<typeof submitFeedback>[0]
  ) {
    await submitFeedback(payload);
    setFeedbackSubmitted(true);
  }

  function handleCloseFeedback() {
    setFeedbackOpen(false);
    setFeedbackSubmitted(false);
  }

  return (
    <section className="min-w-0 space-y-4">
      <PageHeader
        title={MENU_PAGE.TITLE}
        description={MENU_PAGE.DESCRIPTION}
      />

      <TodayMenus
        todayMenus={todayMenus}
        loading={loading}
        error={error}
        onRefresh={fetchTodayMenus}
      />

      <MealFeedbackModal
        feedback={pendingFeedback}
        open={feedbackOpen}
        submitting={submitting}
        submitted={feedbackSubmitted}
        error={feedbackError}
        onClose={handleCloseFeedback}
        onSubmit={handleSubmitFeedback}
      />
    </section>
  );
}
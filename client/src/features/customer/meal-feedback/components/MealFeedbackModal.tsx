import { useEffect, useState } from "react";

import {
  Button,
  Modal,
  Textarea,
} from "@/components/common/ui";

import type {
  PendingMealFeedback,
  SubmitMealFeedbackRequest,
} from "../types/mealFeedback.types";

interface MealFeedbackModalProps {
  feedback: PendingMealFeedback | null;
  open: boolean;
  submitting: boolean;
  submitted: boolean;
  error: string | null;
  onClose: () => void;
  onSubmit: (
    payload: SubmitMealFeedbackRequest
  ) => Promise<void>;
}

const MAX_COMMENT_LENGTH = 1000;

function getMealMessage(
  mealDate: string,
  mealSession: PendingMealFeedback["mealSession"]
) {
  const mealLabel =
    mealSession === "LUNCH" ? "lunch" : "dinner";

  const today = new Date();

  const mealDateObject = new Date(
    `${mealDate}T00:00:00`
  );

  const todayStart = new Date(
    today.getFullYear(),
    today.getMonth(),
    today.getDate()
  );

  const yesterdayStart = new Date(todayStart);

  yesterdayStart.setDate(
    yesterdayStart.getDate() - 1
  );

  if (
    mealDateObject.getTime() ===
    todayStart.getTime()
  ) {
    return `How was today's ${mealLabel}?`;
  }

  if (
    mealDateObject.getTime() ===
    yesterdayStart.getTime()
  ) {
    return `How was yesterday's ${mealLabel}?`;
  }

  const formattedDate =
    mealDateObject.toLocaleDateString("en-IN", {
      day: "numeric",
      month: "short",
    });

  return `How was your ${mealLabel} on ${formattedDate}?`;
}

export default function MealFeedbackModal({
  feedback,
  open,
  submitting,
  submitted,
  error,
  onClose,
  onSubmit,
}: MealFeedbackModalProps) {
  const [ratings, setRatings] = useState<
    Record<number, number>
  >({});

  const [comment, setComment] = useState("");

  useEffect(() => {
    if (!feedback) {
      return;
    }

    const initialRatings: Record<number, number> = {};

    feedback.dishes.forEach((dish) => {
      initialRatings[dish.dishId] = 0;
    });

    setRatings(initialRatings);
    setComment("");
  }, [feedback]);

  function handleRating(
    dishId: number,
    rating: number
  ) {
    if (submitting) {
      return;
    }

    setRatings((previous) => ({
      ...previous,
      [dishId]: rating,
    }));
  }

  function handleClose() {
    if (!submitting) {
      onClose();
    }
  }

  async function handleSubmit() {
    if (
      !feedback ||
      submitting ||
      !allDishesRated
    ) {
      return;
    }

    const payload: SubmitMealFeedbackRequest = {
      mealRecordId: feedback.mealRecordId,

      dishRatings: feedback.dishes.map((dish) => ({
        dishId: dish.dishId,
        rating: ratings[dish.dishId],
      })),

      ...(comment.trim()
        ? {
            commonComment: comment.trim(),
          }
        : {}),
    };

    await onSubmit(payload);
  }

  if (submitted) {
    return (
      <Modal
        open={open}
        onClose={handleClose}
        size="sm"
        footer={
          <Button
            type="button"
            onClick={handleClose}
          >
            Continue
          </Button>
        }
      >
        <div className="flex flex-col items-center px-2 py-4 text-center">
          <div
            className="
              mb-4 flex h-12 w-12 items-center
              justify-center rounded-full
              bg-[var(--color-primary)]/10
              text-xl font-semibold
              text-[var(--color-primary)]
            "
            aria-hidden="true"
          >
            ✓
          </div>

          <h2 className="text-lg font-semibold text-[var(--color-text)]">
            Thanks for your feedback!
          </h2>

          <p className="mt-2 max-w-sm text-sm leading-5 text-[var(--color-text-secondary)]">
            Your feedback helps us make your next
            meal even better.
          </p>
        </div>
      </Modal>
    );
  }

  if (!feedback) {
    return null;
  }

  const allDishesRated = feedback.dishes.every(
    (dish) => (ratings[dish.dishId] ?? 0) >= 1
  );

  return (
    <Modal
      open={open}
      onClose={handleClose}
      title={getMealMessage(
        feedback.mealDate,
        feedback.mealSession
      )}
      size="sm"
      footer={
        <>
          <Button
            type="button"
            variant="secondary"
            disabled={submitting}
            onClick={handleClose}
          >
            Maybe Later
          </Button>

          <Button
            type="button"
            disabled={
              submitting || !allDishesRated
            }
            onClick={() => void handleSubmit()}
          >
            {submitting
              ? "Submitting..."
              : "Submit"}
          </Button>
        </>
      }
    >
      <div className="space-y-5">
        <p className="-mt-3 text-sm leading-5 text-[var(--color-text-secondary)]">
          Rate each dish to share your experience.
        </p>

        <div className="space-y-1">
          {feedback.dishes.map((dish) => {
            const selectedRating =
              ratings[dish.dishId] ?? 0;

            return (
              <div
                key={dish.dishId}
                className="
                  flex min-h-10 items-center
                  justify-between gap-3
                  rounded-lg px-2 py-1
                  hover:bg-[var(--color-background)]
                "
              >
                <p className="min-w-0 break-words text-sm font-medium text-[var(--color-text)]">
                  {dish.dishName}
                </p>

                <div
                  className="flex shrink-0 items-center"
                  role="group"
                  aria-label={`Rate ${dish.dishName}`}
                >
                  {Array.from(
                    { length: 5 },
                    (_, index) => {
                      const rating = index + 1;
                      const selected =
                        rating <= selectedRating;

                      return (
                        <button
                          key={rating}
                          type="button"
                          disabled={submitting}
                          aria-label={`${rating} out of 5 stars`}
                          aria-pressed={selected}
                          onClick={() =>
                            handleRating(
                              dish.dishId,
                              rating
                            )
                          }
                          className="
                            flex h-8 w-7 items-center
                            justify-center
                            rounded-md
                            text-lg leading-none
                            transition-transform
                            hover:scale-110
                            focus:outline-none
                            focus:ring-2
                            focus:ring-amber-400/30
                            disabled:pointer-events-none
                            disabled:opacity-50
                          "
                        >
                          <span
                            className={
                              selected
                                ? "text-amber-400"
                                : "text-[var(--color-text-muted)]"
                            }
                          >
                            {selected
                              ? "★"
                              : "☆"}
                          </span>
                        </button>
                      );
                    }
                  )}
                </div>
              </div>
            );
          })}
        </div>

        <div className="space-y-2">
          <p className="text-sm leading-5 text-[var(--color-text-secondary)]">
            Your feedback helps us make your next
            meal even better.
          </p>

          <Textarea
            id="meal-feedback-comment"
            value={comment}
            onChange={(event) =>
              setComment(event.target.value)
            }
            placeholder="Anything you liked or think we could improve?"
            maxLength={MAX_COMMENT_LENGTH}
            disabled={submitting}
            fullWidth
            rows={2}
          />

          <p className="text-right text-xs text-[var(--color-text-muted)]">
            {comment.length}/{MAX_COMMENT_LENGTH}
          </p>
        </div>

        {error && (
          <p className="rounded-xl border border-[var(--color-danger)]/30 bg-[var(--color-background)] p-3 text-sm text-[var(--color-danger)]">
            {error}
          </p>
        )}
      </div>
    </Modal>
  );
}
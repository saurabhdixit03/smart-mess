import {
  useState,
  type ReactNode,
} from "react";

import {
  CalendarClock,
  CalendarDays,
  Clock3,
  IndianRupee,
  Pencil,
  Plus,
  Trash2,
} from "lucide-react";

import Button from "@/components/common/ui/Button/Button";
import Modal from "@/components/common/ui/Modal/Modal";
import PageHeader from "@/components/common/ui/PageHeader";

import {
  MealPricingForm,
  MessClosureForm,
  MessClosureList,
  ResponseWindowForm,
  WeeklyScheduleForm,
} from "../components";

import {
  useMealPricing,
  useMessClosures,
  useSettings,
} from "../hooks";

import type {
  MealPricingResponse,
  MessClosureResponse,
  UpdateMessClosureRequest,
} from "../types";

type SettingsModal =
  | "initial-pricing"
  | "scheduled-pricing"
  | "response-window"
  | "weekly-schedule"
  | "closure"
  | null;

type SettingsCardProps = {
  icon: ReactNode;
  title: string;
  description: string;
  children: ReactNode;
  onEdit?: () => void;
  disabled?: boolean;
};

const currencyFormat = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

function SettingsCard({
  icon,
  title,
  description,
  children,
  onEdit,
  disabled = false,
}: SettingsCardProps) {
  return (
    <section className="interactive-surface flex min-w-0 flex-col rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
      <div className="flex items-center justify-between gap-3">
        <div className="flex min-w-0 items-center gap-3">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[var(--color-primary)]/10 text-[var(--color-primary)]">
            {icon}
          </div>

          <h2 className="min-w-0 text-base font-semibold tracking-tight text-[var(--color-text)]">
            {title}
          </h2>
        </div>

        {onEdit && (
          <button
            type="button"
            onClick={onEdit}
            disabled={disabled}
            aria-label={`Edit ${title}`}
            title={`Edit ${title}`}
            className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg text-[var(--color-text-secondary)] transition-colors hover:bg-[var(--color-surface-hover)] hover:text-[var(--color-text)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)] disabled:cursor-not-allowed disabled:opacity-40"
          >
            <Pencil size={15} aria-hidden="true" />
          </button>
        )}
      </div>

      <p className="mt-2 text-xs leading-5 text-[var(--color-text-secondary)]">
        {description}
      </p>

      <div className="mt-3 rounded-xl border border-[var(--color-border)] bg-[#FAFAF8] p-3">
        {children}
      </div>
    </section>
  );
}

function SummaryRow({
  label,
  value,
}: {
  label: string;
  value: ReactNode;
}) {
  return (
    <div className="flex items-start justify-between gap-4 text-sm">
      <span className="text-[var(--color-text-secondary)]">
        {label}
      </span>

      <span className="text-right font-medium text-[var(--color-text)]">
        {value}
      </span>
    </div>
  );
}

function PricingSummary({
  pricing,
}: {
  pricing: MealPricingResponse | null;
}) {
  return (
    <div className="space-y-2">
      <SummaryRow
        label="Half Meal"
        value={
          pricing
            ? currencyFormat.format(pricing.halfMealPrice)
            : "—"
        }
      />

      <SummaryRow
        label="Full Meal"
        value={
          pricing
            ? currencyFormat.format(pricing.fullMealPrice)
            : "—"
        }
      />

      <SummaryRow
        label="Extra Roti"
        value={
          pricing
            ? currencyFormat.format(pricing.extraRotiPrice)
            : "—"
        }
      />
    </div>
  );
}

function formatTime(value: string | null): string {
  if (!value) {
    return "Not configured";
  }

  const [hours, minutes] = value.split(":");
  const date = new Date();

  date.setHours(Number(hours), Number(minutes), 0, 0);

  return date.toLocaleTimeString("en-IN", {
    hour: "numeric",
    minute: "2-digit",
  });
}

function formatDay(value: string | null): string {
  if (!value) {
    return "No weekly off";
  }

  return value.charAt(0) + value.slice(1).toLowerCase();
}

function formatDate(value: string): string {
  return new Date(
    `${value.slice(0, 10)}T00:00:00`
  ).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function formatSession(
  value: MessClosureResponse["startSession"]
): string {
  return value === "LUNCH" ? "Lunch" : "Dinner";
}

export default function SettingsPage() {
  const {
    settings,
    loading,
    error: settingsError,
    refreshSettings,
  } = useSettings();

  const {
    pricing,
    scheduledPricing,
    loading: pricingLoading,
    saving: pricingSaving,
    error: pricingError,
    fetchPricing,
    updatePricing,
    cancelScheduledPricing,
  } = useMealPricing();

  const {
    closures,
    loading: closuresLoading,
    saving: closureSaving,
    createClosure,
    updateClosure,
    deleteClosure,
  } = useMessClosures();

  const [activeModal, setActiveModal] =
    useState<SettingsModal>(null);

  const [editingPricing, setEditingPricing] =
    useState<MealPricingResponse | null>(null);

  const [pricingPendingDelete, setPricingPendingDelete] =
    useState<MealPricingResponse | null>(null);

  const [editingClosure, setEditingClosure] =
    useState<MessClosureResponse | null>(null);

  const [closurePendingDelete, setClosurePendingDelete] =
    useState<MessClosureResponse | null>(null);

  const pricingBusy =
    pricingLoading ||
    pricingSaving ||
    Boolean(pricingError);

  const pricingUnavailable =
    pricingBusy || pricing === null;

  function closeModal() {
    if (
      (
        activeModal === "initial-pricing" ||
        activeModal === "scheduled-pricing"
      ) &&
      pricingSaving
    ) {
      return;
    }

    if (activeModal === "closure" && closureSaving) {
      return;
    }

    setActiveModal(null);
    setEditingPricing(null);
    setEditingClosure(null);
  }

  function handlePricingSaved() {
    setActiveModal(null);
    setEditingPricing(null);
  }

  function openUpcomingPricing(
    upcoming: MealPricingResponse | null = null
  ) {
    setEditingPricing(upcoming);
    setActiveModal("scheduled-pricing");
  }

  function closePricingDeleteModal() {
    if (!pricingSaving) {
      setPricingPendingDelete(null);
    }
  }

  async function confirmDeletePricing() {
    if (!pricingPendingDelete || pricingSaving) {
      return;
    }

    const success = await cancelScheduledPricing(
      pricingPendingDelete.mealPricingId
    );

    if (success) {
      setPricingPendingDelete(null);
    }
  }

  function closeClosureDeleteModal() {
    if (!closureSaving) {
      setClosurePendingDelete(null);
    }
  }

  async function handleCreateClosure(
    payload: Parameters<typeof createClosure>[0]
  ) {
    const success = await createClosure(payload);

    if (success) {
      setActiveModal(null);
      setEditingClosure(null);
    }

    return success;
  }

  async function handleUpdateClosure(
    closureId: number,
    payload: UpdateMessClosureRequest
  ) {
    const success = await updateClosure(
      closureId,
      payload
    );

    if (success) {
      setActiveModal(null);
      setEditingClosure(null);
    }

    return success;
  }

  function handleEditClosure(
    closure: MessClosureResponse
  ) {
    setEditingClosure(closure);
    setActiveModal("closure");
  }

  async function handleDeleteClosure(
    closureId: number
  ) {
    const closure = closures.find(
      (item) => item.closureId === closureId
    );

    if (!closure) {
      return false;
    }

    setClosurePendingDelete(closure);
    return false;
  }

  async function confirmDeleteClosure() {
    if (!closurePendingDelete || closureSaving) {
      return;
    }

    const success = await deleteClosure(
      closurePendingDelete.closureId
    );

    if (success) {
      setClosurePendingDelete(null);
    }
  }

  function handleScheduleClosure() {
    setEditingClosure(null);
    setActiveModal("closure");
  }

  async function handleSettingsSaved() {
    setActiveModal(null);
    await refreshSettings();
  }

  const closedSessions: string[] = [];

  if (settings?.weeklyLunchClosed) {
    closedSessions.push("Lunch");
  }

  if (settings?.weeklyDinnerClosed) {
    closedSessions.push("Dinner");
  }

  const weeklySessionSummary =
    closedSessions.length > 0
      ? closedSessions.join(" & ")
      : "None";

  const sortedScheduledPricing = [...scheduledPricing].sort(
    (first, second) =>
      first.effectiveFrom.localeCompare(second.effectiveFrom)
  );

  if (
    (loading && !settings) ||
    (pricingLoading && !pricing)
  ) {
    return (
      <div className="p-6">
        Loading settings...
      </div>
    );
  }

  return (
    <div className="space-y-5">
      <PageHeader
        title="Settings"
        description="Manage mess operations, schedules, and meal pricing."
      />

      {settingsError && (
        <div
          role="alert"
          className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-red-200 p-4"
        >
          <p className="text-sm text-red-500">
            {settingsError}
          </p>

          <Button
            variant="secondary"
            size="sm"
            disabled={loading}
            onClick={() => void refreshSettings()}
          >
            Retry Settings
          </Button>
        </div>
      )}

      {pricingError && (
        <div
          role="alert"
          className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-red-200 p-4"
        >
          <p className="text-sm text-red-500">
            {pricingError}
          </p>

          <Button
            variant="secondary"
            size="sm"
            disabled={pricingLoading || pricingSaving}
            onClick={() => void fetchPricing()}
          >
            Retry Pricing
          </Button>
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <SettingsCard
          icon={<Clock3 size={18} />}
          title="Response Window"
          description="Customer meal response cutoffs."
          disabled={loading || Boolean(settingsError)}
          onEdit={() => setActiveModal("response-window")}
        >
          <div className="space-y-2">
            <SummaryRow
              label="Lunch"
              value={formatTime(
                settings?.lunchResponseCutoff ?? null
              )}
            />

            <SummaryRow
              label="Dinner"
              value={formatTime(
                settings?.dinnerResponseCutoff ?? null
              )}
            />
          </div>
        </SettingsCard>

        <SettingsCard
          icon={<CalendarDays size={18} />}
          title="Weekly Schedule"
          description="Recurring weekly closure."
          disabled={loading || Boolean(settingsError)}
          onEdit={() => setActiveModal("weekly-schedule")}
        >
          <div className="space-y-2">
            <SummaryRow
              label="Closed Day"
              value={formatDay(
                settings?.weeklyClosedDay ?? null
              )}
            />

            <SummaryRow
              label="Closed Meals"
              value={weeklySessionSummary}
            />
          </div>
        </SettingsCard>

        <SettingsCard
          icon={<IndianRupee size={18} />}
          title="Meal Pricing"
          description={
            pricing
              ? "Current prices for newly recorded meals."
              : "Set your meal prices before recording collections."
          }
        >
          {pricing ? (
            <PricingSummary pricing={pricing} />
          ) : pricingError ? (
            <p className="text-sm text-[var(--color-text-secondary)]">
              Meal prices could not be loaded. Use Retry Pricing above.
            </p>
          ) : (
            <div className="space-y-3">
              <p className="text-sm text-[var(--color-text-secondary)]">
                Meal prices not configured.
              </p>

              <Button
                type="button"
                size="sm"
                disabled={pricingBusy}
                onClick={() => {
                  setEditingPricing(null);
                  setActiveModal("initial-pricing");
                }}
              >
                <Plus size={16} />
                Set Meal Prices
              </Button>
            </div>
          )}
        </SettingsCard>
      </div>

      <div className="grid items-start gap-4 lg:grid-cols-2">
        <section className="interactive-surface min-w-0 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <div className="flex items-center gap-2">
                <CalendarClock
                  size={18}
                  className="shrink-0 text-[var(--color-primary)]"
                />

                <h2 className="text-base font-semibold text-[var(--color-text)]">
                  Temporary Mess Closures
                </h2>
              </div>

              <p className="mt-1 text-xs leading-5 text-[var(--color-text-secondary)]">
                Schedule and manage temporary operational breaks.
              </p>
            </div>

            <Button
              type="button"
              size="sm"
              disabled={closureSaving}
              onClick={handleScheduleClosure}
            >
              <Plus size={16} />
              Schedule Closure
            </Button>
          </div>

          <div className="mt-5 border-t border-[var(--color-border)] pt-5">
            <MessClosureList
              closures={closures}
              loading={closuresLoading}
              saving={closureSaving}
              onEdit={handleEditClosure}
              onDelete={handleDeleteClosure}
            />
          </div>
        </section>

        <section className="interactive-surface min-w-0 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <div className="flex items-center gap-2">
                <CalendarClock
                  size={18}
                  className="shrink-0 text-[var(--color-primary)]"
                />

                <h2 className="text-base font-semibold text-[var(--color-text)]">
                  Upcoming Meal Pricing
                </h2>
              </div>

              <p className="mt-1 text-xs leading-5 text-[var(--color-text-secondary)]">
                Plan and manage your next meal price change.
              </p>
            </div>

            {scheduledPricing.length === 0 && pricing !== null && (
              <Button
                type="button"
                size="sm"
                disabled={pricingUnavailable}
                onClick={() => openUpcomingPricing()}
              >
                <Plus size={16} />
                Plan Price Change
              </Button>
            )}
          </div>

          <div className="mt-5 border-t border-[var(--color-border)] pt-5">
            {pricingLoading ? (
              <div className="py-8 text-center text-sm text-[var(--color-text-secondary)]">
                Loading upcoming prices...
              </div>
            ) : pricingError ? (
              <div className="py-8 text-center text-sm text-[var(--color-text-secondary)]">
                Upcoming prices could not be refreshed.
                Use Retry Pricing above.
              </div>
            ) : sortedScheduledPricing.length === 0 ? (
              <div className="rounded-[var(--radius-lg)] border border-dashed border-[var(--color-border)] px-5 py-8 text-center">
                <IndianRupee
                  size={26}
                  className="mx-auto mb-3 text-[var(--color-text-secondary)]"
                />

                <p className="text-sm font-medium text-[var(--color-text)]">
                  {pricing
                    ? "No upcoming price change"
                    : "Set your initial meal prices"}
                </p>

                <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
                  {pricing
                    ? "Planned meal price changes will appear here."
                    : "Use Set Meal Prices above before planning a future change."}
                </p>
              </div>
            ) : (
              <div className="space-y-3">
                {sortedScheduledPricing.length > 1 && (
                  <p
                    role="alert"
                    className="rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs leading-5 text-amber-900"
                  >
                    Multiple changes were saved previously.
                    Delete the extra entries to keep one
                    upcoming price change.
                  </p>
                )}

                {sortedScheduledPricing.map((item) => (
                  <div
                    key={item.mealPricingId}
                    className="rounded-[var(--radius-lg)] border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3.5"
                  >
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                      <div className="min-w-0">
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="rounded-full bg-[var(--color-primary)]/10 px-2.5 py-1 text-xs font-semibold text-[var(--color-primary)]">
                            Price Change
                          </span>

                          <p className="text-sm font-semibold text-[var(--color-text)]">
                            From {formatDate(item.effectiveFrom)}
                          </p>
                        </div>

                        <p className="mt-2 text-sm leading-6 text-[var(--color-text-secondary)]">
                          Full {currencyFormat.format(item.fullMealPrice)}
                          {" · "}
                          Half {currencyFormat.format(item.halfMealPrice)}
                          {" · "}
                          Extra roti {currencyFormat.format(item.extraRotiPrice)}
                        </p>
                      </div>

                      <div className="flex shrink-0 gap-2">
                        <Button
                          type="button"
                          variant="secondary"
                          size="sm"
                          disabled={
                            pricingUnavailable ||
                            sortedScheduledPricing.length > 1
                          }
                          onClick={() => openUpcomingPricing(item)}
                        >
                          <Pencil size={14} />
                          Edit
                        </Button>

                        <Button
                          type="button"
                          variant="danger"
                          size="sm"
                          disabled={pricingBusy}
                          onClick={() => setPricingPendingDelete(item)}
                        >
                          <Trash2 size={14} />
                          Delete
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>
      </div>

      <Modal
        open={activeModal === "initial-pricing"}
        title="Set Meal Prices"
        size="lg"
        onClose={closeModal}
      >
        {activeModal === "initial-pricing" && (
          <MealPricingForm
            pricing={null}
            saving={pricingSaving}
            mode="IMMEDIATE"
            onUpdate={updatePricing}
            onSuccess={handlePricingSaved}
          />
        )}
      </Modal>

      <Modal
        open={activeModal === "scheduled-pricing"}
        title={
          editingPricing
            ? "Edit Upcoming Price Change"
            : "Plan Price Change"
        }
        size="lg"
        onClose={closeModal}
      >
        {activeModal === "scheduled-pricing" && (
          <MealPricingForm
            pricing={editingPricing ?? pricing}
            saving={pricingSaving}
            mode={
              editingPricing
                ? "EDIT_SCHEDULED"
                : "SCHEDULED"
            }
            onUpdate={updatePricing}
            onSuccess={handlePricingSaved}
          />
        )}
      </Modal>

      <Modal
        open={pricingPendingDelete !== null}
        title="Delete Upcoming Price Change"
        size="sm"
        onClose={closePricingDeleteModal}
        footer={
          <>
            <Button
              type="button"
              variant="secondary"
              disabled={pricingSaving}
              onClick={closePricingDeleteModal}
            >
              Cancel
            </Button>

            <Button
              type="button"
              variant="danger"
              disabled={pricingSaving || !pricingPendingDelete}
              onClick={() => void confirmDeletePricing()}
            >
              {pricingSaving ? "Deleting..." : "Delete Price Change"}
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          <p className="text-sm leading-6 text-[var(--color-text-secondary)]">
            Are you sure you want to delete this upcoming
            price change? Customers will be notified
            that it will no longer apply.
          </p>

          {pricingPendingDelete && (
            <div className="rounded-lg border border-[var(--color-border)] bg-[var(--color-surface-hover)] p-4">
              <p className="mb-3 text-sm font-semibold">
                From {formatDate(pricingPendingDelete.effectiveFrom)}
              </p>

              <PricingSummary pricing={pricingPendingDelete} />
            </div>
          )}

          <p className="text-xs text-[var(--color-text-secondary)]">
            This action cannot be undone.
          </p>
        </div>
      </Modal>

      <Modal
        open={activeModal === "response-window"}
        title="Edit Response Window"
        size="lg"
        onClose={closeModal}
      >
        {activeModal === "response-window" && (
          <ResponseWindowForm
            settings={settings}
            onSuccess={handleSettingsSaved}
          />
        )}
      </Modal>

      <Modal
        open={activeModal === "weekly-schedule"}
        title="Edit Weekly Schedule"
        size="lg"
        onClose={closeModal}
      >
        {activeModal === "weekly-schedule" && (
          <WeeklyScheduleForm
            settings={settings}
            onSuccess={handleSettingsSaved}
          />
        )}
      </Modal>

      <Modal
        open={activeModal === "closure"}
        title={
          editingClosure
            ? "Edit Temporary Closure"
            : "Schedule Temporary Closure"
        }
        size="lg"
        onClose={closeModal}
      >
        {activeModal === "closure" && (
          <MessClosureForm
            closure={editingClosure}
            saving={closureSaving}
            onCreate={handleCreateClosure}
            onUpdate={handleUpdateClosure}
            onCancelEdit={closeModal}
          />
        )}
      </Modal>

      <Modal
        open={closurePendingDelete !== null}
        title="Delete Temporary Closure"
        size="sm"
        onClose={closeClosureDeleteModal}
        footer={
          <>
            <Button
              type="button"
              variant="secondary"
              disabled={closureSaving}
              onClick={closeClosureDeleteModal}
            >
              Cancel
            </Button>

            <Button
              type="button"
              variant="danger"
              disabled={closureSaving}
              onClick={() => void confirmDeleteClosure()}
            >
              {closureSaving ? "Deleting..." : "Delete Closure"}
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          <p className="text-sm leading-6 text-[var(--color-text-secondary)]">
            Are you sure you want to delete this temporary closure?
          </p>

          {closurePendingDelete && (
            <div className="rounded-lg border border-[var(--color-border)] bg-[var(--color-surface-hover)] p-4">
              <p className="text-sm font-semibold text-[var(--color-text)]">
                {formatDate(closurePendingDelete.startDate)}
                {" · "}
                {formatSession(closurePendingDelete.startSession)}
                {" → "}
                {formatDate(closurePendingDelete.endDate)}
                {" · "}
                {formatSession(closurePendingDelete.endSession)}
              </p>

              <p className="mt-2 text-sm leading-5 text-[var(--color-text-secondary)]">
                {closurePendingDelete.reason}
              </p>
            </div>
          )}

          <p className="text-xs text-[var(--color-text-secondary)]">
            This action cannot be undone.
          </p>
        </div>
      </Modal>
    </div>
  );
}
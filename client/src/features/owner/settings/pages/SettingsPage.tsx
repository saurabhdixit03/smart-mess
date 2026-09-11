import {
  useState,
  type ReactNode,
} from "react";

import {
  CalendarClock,
  CalendarDays,
  Clock3,
  CreditCard,
  IndianRupee,
  Pencil,
  Plus,
} from "lucide-react";

import Button from "@/components/common/ui/Button/Button";
import Modal from "@/components/common/ui/Modal/Modal";
import PageHeader from "@/components/common/ui/PageHeader";

import {
  MealPricingForm,
  MessClosureForm,
  MessClosureList,
  ResponseWindowForm,
  SettingsForm,
  WeeklyScheduleForm,
} from "../components";

import {
  useMealPricing,
  useMessClosures,
  useSettings,
} from "../hooks";

import type {
  MessClosureResponse,
  UpdateMessClosureRequest,
} from "../types";

type SettingsModal =
  | "pricing"
  | "response-window"
  | "weekly-schedule"
  | "payment"
  | "closure"
  | null;

type SettingsCardProps = {
  icon: ReactNode;
  title: string;
  description: string;
  children: ReactNode;
  onEdit: () => void;
};

function SettingsCard({
  icon,
  title,
  description,
  children,
  onEdit,
}: SettingsCardProps) {
  return (
    <section
      className="
        flex
        min-h-[190px]
        flex-col
        rounded-xl
        border
        border-[var(--color-border)]
        bg-[var(--color-surface)]
        p-5
      "
    >
      <div className="flex items-start justify-between gap-4">
        <div
          className="
            flex
            h-9
            w-9
            shrink-0
            items-center
            justify-center
            rounded-lg
            bg-[var(--color-primary)]/10
            text-[var(--color-primary)]
          "
        >
          {icon}
        </div>

        <Button
          type="button"
          variant="secondary"
          size="sm"
          onClick={onEdit}
        >
          <Pencil size={14} />
          Edit
        </Button>
      </div>

      <div className="mt-4">
        <h2
          className="
            text-base
            font-semibold
            text-[var(--color-text)]
          "
        >
          {title}
        </h2>

        <p
          className="
            mt-1
            text-xs
            leading-5
            text-[var(--color-text-secondary)]
          "
        >
          {description}
        </p>
      </div>

      <div className="mt-auto pt-4">
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
    <div
      className="
        flex
        items-center
        justify-between
        gap-4
        text-sm
      "
    >
      <span className="text-[var(--color-text-secondary)]">
        {label}
      </span>

      <span
        className="
          truncate
          font-medium
          text-[var(--color-text)]
        "
      >
        {value}
      </span>
    </div>
  );
}

function formatTime(
  value: string | null
) {
  if (!value) {
    return "Not configured";
  }

  const [hours, minutes] =
    value.split(":");

  const date = new Date();

  date.setHours(
    Number(hours),
    Number(minutes),
    0,
    0
  );

  return date.toLocaleTimeString(
    undefined,
    {
      hour: "numeric",
      minute: "2-digit",
    }
  );
}

function formatDay(
  value: string | null
) {
  if (!value) {
    return "No weekly off";
  }

  return (
    value.charAt(0) +
    value
      .slice(1)
      .toLowerCase()
  );
}

function formatDate(
  value: string
) {
  return new Date(
    `${value}T00:00:00`
  ).toLocaleDateString(
    undefined,
    {
      day: "2-digit",
      month: "short",
      year: "numeric",
    }
  );
}

function formatSession(
  value:
    | MessClosureResponse["startSession"]
    | MessClosureResponse["endSession"]
) {
  return value === "LUNCH"
    ? "Lunch"
    : "Dinner";
}

export default function SettingsPage() {
  const {
    settings,
    loading,
    refreshSettings,
  } = useSettings();

  const {
    pricing,
    loading: pricingLoading,
    saving: pricingSaving,
    updatePricing,
  } = useMealPricing();

  const {
    closures,
    loading: closuresLoading,
    saving: closureSaving,
    createClosure,
    updateClosure,
    deleteClosure,
  } = useMessClosures();

  const [
    activeModal,
    setActiveModal,
  ] = useState<SettingsModal>(
    null
  );

  const [
    editingClosure,
    setEditingClosure,
  ] = useState<MessClosureResponse | null>(
    null
  );

  const [
    closurePendingDelete,
    setClosurePendingDelete,
  ] = useState<MessClosureResponse | null>(
    null
  );

  const closeModal = () => {
    setActiveModal(null);
    setEditingClosure(null);
  };

  const closeDeleteModal = () => {
    if (closureSaving) {
      return;
    }

    setClosurePendingDelete(null);
  };

  const handleCreateClosure = async (
    payload: Parameters<
      typeof createClosure
    >[0]
  ) => {
    const success =
      await createClosure(payload);

    if (success) {
      closeModal();
    }

    return success;
  };

  const handleUpdateClosure = async (
    closureId: number,
    payload: UpdateMessClosureRequest
  ) => {
    const success =
      await updateClosure(
        closureId,
        payload
      );

    if (success) {
      closeModal();
    }

    return success;
  };

  const handleEditClosure = (
    closure: MessClosureResponse
  ) => {
    setEditingClosure(closure);
    setActiveModal("closure");
  };

  const handleDeleteClosure = async (
    closureId: number
  ) => {
    const closure =
      closures.find(
        (item) =>
          item.closureId ===
          closureId
      );

    if (!closure) {
      return false;
    }

    setClosurePendingDelete(
      closure
    );

    return false;
  };

  const confirmDeleteClosure =
    async () => {
      if (!closurePendingDelete) {
        return;
      }

      const success =
        await deleteClosure(
          closurePendingDelete.closureId
        );

      if (success) {
        setClosurePendingDelete(
          null
        );
      }
    };

  const handleScheduleClosure = () => {
    setEditingClosure(null);
    setActiveModal("closure");
  };

  if (loading || pricingLoading) {
    return (
      <div className="p-6">
        Loading settings...
      </div>
    );
  }

  const closedSessions: string[] =
    [];

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

  return (
    <div className="space-y-5">
      <PageHeader
        title="Settings"
        description="Manage mess operations, schedules, pricing, and payments."
      />

      {/* Settings Overview */}
      <div
        className="
          grid
          gap-4
          md:grid-cols-2
          xl:grid-cols-4
        "
      >
        <SettingsCard
          icon={
            <Clock3 size={18} />
          }
          title="Response Window"
          description="Customer meal response cutoffs."
          onEdit={() =>
            setActiveModal(
              "response-window"
            )
          }
        >
          <div className="space-y-2">
            <SummaryRow
              label="Lunch"
              value={formatTime(
                settings
                  ?.lunchResponseCutoff ??
                  null
              )}
            />

            <SummaryRow
              label="Dinner"
              value={formatTime(
                settings
                  ?.dinnerResponseCutoff ??
                  null
              )}
            />
          </div>
        </SettingsCard>

        <SettingsCard
          icon={
            <CalendarDays
              size={18}
            />
          }
          title="Weekly Schedule"
          description="Recurring weekly closure."
          onEdit={() =>
            setActiveModal(
              "weekly-schedule"
            )
          }
        >
          <div className="space-y-2">
            <SummaryRow
              label="Closed Day"
              value={formatDay(
                settings
                  ?.weeklyClosedDay ??
                  null
              )}
            />

            <SummaryRow
              label="Closed Meals"
              value={
                weeklySessionSummary
              }
            />
          </div>
        </SettingsCard>

        <SettingsCard
          icon={
            <IndianRupee size={18} />
          }
          title="Meal Pricing"
          description="Prices used for meal billing."
          onEdit={() =>
            setActiveModal(
              "pricing"
            )
          }
        >
          <div className="space-y-2">
            <SummaryRow
              label="Half Meal"
              value={
                pricing
                  ? `₹${pricing.halfMealPrice}`
                  : "—"
              }
            />

            <SummaryRow
              label="Full Meal"
              value={
                pricing
                  ? `₹${pricing.fullMealPrice}`
                  : "—"
              }
            />

            <SummaryRow
              label="Extra Roti"
              value={
                pricing
                  ? `₹${pricing.extraRotiPrice}`
                  : "—"
              }
            />
          </div>
        </SettingsCard>

        <SettingsCard
          icon={
            <CreditCard size={18} />
          }
          title="UPI Payment"
          description="Customer payment destination."
          onEdit={() =>
            setActiveModal(
              "payment"
            )
          }
        >
          <div className="space-y-2">
            <SummaryRow
              label="UPI ID"
              value={
                settings?.upiId ||
                "Not configured"
              }
            />

            <SummaryRow
              label="Receiver"
              value={
                settings
                  ?.receiverName ||
                "Not configured"
              }
            />
          </div>
        </SettingsCard>
      </div>

      {/* Temporary Closures */}
      <section
        className="
          rounded-xl
          border
          border-[var(--color-border)]
          bg-[var(--color-surface)]
          p-5
        "
      >
        <div
          className="
            flex
            flex-col
            gap-4
            sm:flex-row
            sm:items-center
            sm:justify-between
          "
        >
          <div>
            <div
              className="
                flex
                items-center
                gap-2
              "
            >
              <CalendarClock
                size={18}
                className="text-[var(--color-primary)]"
              />

              <h2
                className="
                  text-base
                  font-semibold
                  text-[var(--color-text)]
                "
              >
                Temporary Mess Closures
              </h2>
            </div>

            <p
              className="
                mt-1
                text-xs
                text-[var(--color-text-secondary)]
              "
            >
              Schedule and manage temporary operational breaks.
            </p>
          </div>

          <Button
            type="button"
            size="sm"
            onClick={
              handleScheduleClosure
            }
          >
            <Plus size={16} />
            Schedule Closure
          </Button>
        </div>

        <div
          className="
            mt-5
            border-t
            border-[var(--color-border)]
            pt-5
          "
        >
          <MessClosureList
            closures={closures}
            loading={
              closuresLoading
            }
            saving={closureSaving}
            onEdit={
              handleEditClosure
            }
            onDelete={
              handleDeleteClosure
            }
          />
        </div>
      </section>

      {/* Meal Pricing Modal */}
      <Modal
        open={
          activeModal === "pricing"
        }
        title="Edit Meal Pricing"
        size="lg"
        onClose={closeModal}
      >
        <MealPricingForm
          pricing={pricing}
          saving={pricingSaving}
          onUpdate={
            updatePricing
          }
        />
      </Modal>

      {/* Response Window Modal */}
      <Modal
        open={
          activeModal ===
          "response-window"
        }
        title="Edit Response Window"
        size="lg"
        onClose={closeModal}
      >
        <ResponseWindowForm
          settings={settings}
          onSuccess={
            refreshSettings
          }
        />
      </Modal>

      {/* Weekly Schedule Modal */}
      <Modal
        open={
          activeModal ===
          "weekly-schedule"
        }
        title="Edit Weekly Schedule"
        size="lg"
        onClose={closeModal}
      >
        <WeeklyScheduleForm
          settings={settings}
          onSuccess={
            refreshSettings
          }
        />
      </Modal>

      {/* Payment Settings Modal */}
      <Modal
        open={
          activeModal === "payment"
        }
        title="Edit UPI Payment"
        size="md"
        onClose={closeModal}
      >
        <SettingsForm
          settings={settings}
          onSuccess={
            refreshSettings
          }
        />
      </Modal>

      {/* Temporary Closure Modal */}
      <Modal
        open={
          activeModal === "closure"
        }
        title={
          editingClosure
            ? "Edit Temporary Closure"
            : "Schedule Temporary Closure"
        }
        size="xl"
        onClose={closeModal}
      >
        <MessClosureForm
          closure={editingClosure}
          saving={closureSaving}
          onCreate={
            handleCreateClosure
          }
          onUpdate={
            handleUpdateClosure
          }
          onCancelEdit={
            closeModal
          }
        />
      </Modal>

      {/* Delete Closure Confirmation Modal */}
      <Modal
        open={
          closurePendingDelete !==
          null
        }
        title="Delete Temporary Closure"
        size="sm"
        onClose={closeDeleteModal}
      >
        <div className="space-y-5">
          <p
            className="
              text-sm
              leading-6
              text-[var(--color-text-secondary)]
            "
          >
            Are you sure you want to delete this temporary closure?
          </p>

          {closurePendingDelete && (
            <div
              className="
                rounded-lg
                border
                border-[var(--color-border)]
                bg-[var(--color-surface-hover)]
                p-4
              "
            >
              <p
                className="
                  text-sm
                  font-semibold
                  text-[var(--color-text)]
                "
              >
                {formatDate(
                  closurePendingDelete.startDate
                )}
                {" · "}
                {formatSession(
                  closurePendingDelete.startSession
                )}
                {" → "}
                {formatDate(
                  closurePendingDelete.endDate
                )}
                {" · "}
                {formatSession(
                  closurePendingDelete.endSession
                )}
              </p>

              <p
                className="
                  mt-2
                  text-sm
                  leading-5
                  text-[var(--color-text-secondary)]
                "
              >
                {
                  closurePendingDelete.reason
                }
              </p>
            </div>
          )}

          <p
            className="
              text-xs
              text-[var(--color-text-secondary)]
            "
          >
            This action cannot be undone.
          </p>

          <div className="flex justify-end gap-3">
            <Button
              type="button"
              variant="secondary"
              disabled={closureSaving}
              onClick={
                closeDeleteModal
              }
            >
              Cancel
            </Button>

            <Button
              type="button"
              variant="danger"
              disabled={closureSaving}
              onClick={
                confirmDeleteClosure
              }
            >
              {closureSaving
                ? "Deleting..."
                : "Delete Closure"}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
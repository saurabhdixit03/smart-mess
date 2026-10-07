import { useMemo, useState } from "react";
import { toast } from "sonner";

import {
  ArrowRight,
  Clock3,
  QrCode,
  RefreshCw,
  Search,
  Users,
  UserCheck,
  UserX,
  X,
} from "lucide-react";

import {
  CustomerForm,
  CustomerTable,
} from "../components";

import CustomerRegistrationLinkModal from "../components/CustomerRegistrationLinkModal";

import { useCustomers } from "../hooks/useCustomers";
import { customerApi } from "../api/customer.api";

import type { CustomerResponse } from "../types/customer.types";

import Button from "@/components/common/ui/Button/Button";
import Input from "@/components/common/ui/Input/Input";
import Modal from "@/components/common/ui/Modal/Modal";
import PageHeader from "@/components/common/ui/PageHeader";
import StatsCard from "@/components/common/ui/StatsCard";
import SearchToolbar from "@/components/common/ui/SearchToolbar";

type CustomerAction =
  | "approve"
  | "reject"
  | "reactivate"
  | "deactivate";

type TableStatusFilter =
  | "ACTIVE"
  | "INACTIVE"
  | "ALL";

type PendingAction = {
  type: CustomerAction;
  customer: CustomerResponse;
};

const ACTION_DETAILS = {
  approve: {
    title: "Approve Customer",
    button: "Approve",
    progress: "Approving...",
    success: "Customer approved successfully.",
    explanation:
      "The customer will become active and can sign in. An approval email with the login link will be sent.",
  },
  reject: {
    title: "Reject Registration",
    button: "Reject and Remove",
    progress: "Removing...",
    success: "Registration rejected and removed.",
    explanation:
      "This pending registration will be permanently deleted. No email will be sent. The person can register again later.",
  },
  reactivate: {
    title: "Reactivate Customer",
    button: "Reactivate",
    progress: "Reactivating...",
    success: "Customer reactivated successfully.",
    explanation:
      "The customer can sign in again. Their existing meal and billing history will remain available.",
  },
  deactivate: {
    title: "Deactivate Customer",
    button: "Deactivate",
    progress: "Deactivating...",
    success: "Customer deactivated successfully.",
    explanation:
      "The customer will become inactive and cannot participate in daily meal operations. They can still sign in to view their history and pay outstanding bills.",
  },
} as const;

function newestFirst(
  first: CustomerResponse,
  second: CustomerResponse
): number {
  const firstDate =
    first.createdAt || first.joiningDate;

  const secondDate =
    second.createdAt || second.joiningDate;

  return (
    secondDate.localeCompare(firstDate) ||
    second.customerId - first.customerId
  );
}

function isRegistrationAction(
  type: CustomerAction
): boolean {
  return type === "approve" || type === "reject";
}

export default function CustomerPage() {
  const {
    customers,
    loading,
    error,
    fetchCustomers,
  } = useCustomers();

  const [selectedCustomer, setSelectedCustomer] =
    useState<CustomerResponse | null>(null);

  const [isManageOpen, setIsManageOpen] =
    useState(false);

  const [isRegistrationOpen, setIsRegistrationOpen] =
    useState(false);

  const [isPendingReviewOpen, setIsPendingReviewOpen] =
    useState(false);

  const [pendingAction, setPendingAction] =
    useState<PendingAction | null>(null);

  const [savingAction, setSavingAction] =
    useState(false);

  const [actionError, setActionError] =
    useState<string | null>(null);

  const [search, setSearch] = useState("");

  const [statusFilter, setStatusFilter] =
    useState<TableStatusFilter>("ALL");

  const [currentPage, setCurrentPage] =
    useState(1);

  const [rowsPerPage, setRowsPerPage] =
    useState(10);

  const paginationKey = JSON.stringify([
    search,
    statusFilter,
    rowsPerPage,
  ]);

  const [previousPaginationKey, setPreviousPaginationKey] =
    useState(paginationKey);

  const pendingCustomers = useMemo(
    () =>
      customers
        .filter(
          (customer) => customer.status === "PENDING"
        )
        .sort(newestFirst),
    [customers]
  );

  const activeCustomers = customers.filter(
    (customer) => customer.status === "ACTIVE"
  ).length;

  const inactiveCustomers = customers.filter(
    (customer) => customer.status === "INACTIVE"
  ).length;

  const filteredCustomers = useMemo(() => {
    const keyword = search.trim().toLowerCase();

    return customers
      .filter((customer) => {
        if (
          customer.status !== "ACTIVE" &&
          customer.status !== "INACTIVE"
        ) {
          return false;
        }

        const matchesStatus =
          statusFilter === "ALL" ||
          customer.status === statusFilter;

        const matchesSearch =
          !keyword ||
          customer.fullName.toLowerCase().includes(keyword) ||
          customer.mobileNumber.includes(keyword) ||
          (customer.email ?? "")
            .toLowerCase()
            .includes(keyword);

        return matchesStatus && matchesSearch;
      })
      .sort(newestFirst);
  }, [customers, search, statusFilter]);

  const totalPages = Math.max(
    1,
    Math.ceil(filteredCustomers.length / rowsPerPage)
  );

  const paginatedCustomers = useMemo(() => {
    const start =
      (currentPage - 1) * rowsPerPage;

    return filteredCustomers.slice(
      start,
      start + rowsPerPage
    );
  }, [
    filteredCustomers,
    currentPage,
    rowsPerPage,
  ]);

  // Keep pagination valid before committing the updated table.
  if (previousPaginationKey !== paginationKey) {
    setPreviousPaginationKey(paginationKey);
    setCurrentPage(1);
  } else if (currentPage > totalPages) {
    setCurrentPage(totalPages);
  }

  function handleManageCustomer(
    customer: CustomerResponse
  ) {
    setSelectedCustomer(customer);
    setIsManageOpen(true);
  }

  function handleCancel() {
    setSelectedCustomer(null);
    setIsManageOpen(false);
  }

  async function handleSuccess() {
    await fetchCustomers();

    setSelectedCustomer(null);
    setIsManageOpen(false);
  }

  function openAction(
    type: CustomerAction,
    customer: CustomerResponse
  ) {
    if (isRegistrationAction(type)) {
      setIsPendingReviewOpen(false);
    }

    setActionError(null);
    setPendingAction({ type, customer });
  }

  function closeAction() {
    if (savingAction) {
      return;
    }

    const returnToReview =
      pendingAction !== null &&
      isRegistrationAction(pendingAction.type);

    setPendingAction(null);
    setActionError(null);

    if (returnToReview) {
      setIsPendingReviewOpen(true);
    }
  }

  async function confirmAction() {
    if (!pendingAction || savingAction) {
      return;
    }

    const { type, customer } = pendingAction;

    try {
      setSavingAction(true);
      setActionError(null);

      switch (type) {
        case "approve":
          await customerApi.approveCustomer(
            customer.customerId
          );
          break;

        case "reject":
          await customerApi.rejectCustomer(
            customer.customerId
          );
          break;

        case "reactivate":
          await customerApi.reactivateCustomer(
            customer.customerId
          );
          break;

        case "deactivate":
          await customerApi.deleteCustomer(
            customer.customerId
          );
          break;
      }

      toast.success(ACTION_DETAILS[type].success);

      await fetchCustomers();

      setPendingAction(null);

      if (isRegistrationAction(type)) {
        setIsPendingReviewOpen(true);
      }
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "Unable to update customer status.";

      setActionError(message);
      toast.error(message);
    } finally {
      setSavingAction(false);
    }
  }

  const actionDetails = pendingAction
    ? ACTION_DETAILS[pendingAction.type]
    : null;

  const isDestructiveAction =
    pendingAction?.type === "reject" ||
    pendingAction?.type === "deactivate";

  const showRegistrationDetails =
    pendingAction !== null &&
    isRegistrationAction(pendingAction.type);

  const reviewDisabled =
    loading ||
    savingAction ||
    Boolean(error) ||
    pendingCustomers.length === 0;

  return (
    <>
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <PageHeader
            title="Customers"
            description="Review registrations and manage your customers."
          />

          <div className="flex flex-wrap items-center gap-2">
            <Button
              variant="outline"
              disabled={loading || savingAction}
              title="Fetch the latest customers and registrations"
              onClick={() => void fetchCustomers()}
            >
              <RefreshCw
                size={16}
                className={loading ? "animate-spin" : ""}
              />
              Refresh
            </Button>

            <Button
              onClick={() => setIsRegistrationOpen(true)}
            >
              <QrCode size={18} />
              Registration QR
            </Button>
          </div>
        </div>

        <div className="grid items-stretch gap-5 md:grid-cols-2 xl:grid-cols-4">
          <div className="h-full [&>*]:h-full">
            <StatsCard
              title="Total Customers"
              value={activeCustomers + inactiveCustomers}
              description="Approved customer accounts"
              icon={<Users size={26} />}
            />
          </div>

          <div className="h-full [&>*]:h-full">
            <StatsCard
              title="Active Customers"
              value={activeCustomers}
              description="Currently active"
              icon={<UserCheck size={26} />}
            />
          </div>

          <div className="h-full [&>*]:h-full">
            <StatsCard
              title="Inactive Customers"
              value={inactiveCustomers}
              description="Currently inactive"
              icon={<UserX size={26} />}
            />
          </div>

          <section
            aria-labelledby="pending-card-title"
            className="flex h-full flex-col rounded-2xl border border-amber-200 bg-amber-50 p-5"
          >
            <div className="flex items-start justify-between gap-3">
              <div>
                <h2
                  id="pending-card-title"
                  className="text-sm font-medium text-amber-900"
                >
                  Pending Registrations
                </h2>

                <p className="mt-2 text-3xl font-semibold text-amber-950">
                  {pendingCustomers.length}
                </p>
              </div>

              <div className="rounded-xl bg-amber-100 p-3 text-amber-700">
                <Clock3 size={26} />
              </div>
            </div>

            <div className="mt-auto flex flex-wrap items-center justify-between gap-2 pt-3">
              <p className="text-xs text-amber-800">
                {pendingCustomers.length > 0
                  ? "Awaiting your review"
                  : "No pending registrations"}
              </p>

              <button
                type="button"
                disabled={reviewDisabled}
                onClick={() => setIsPendingReviewOpen(true)}
                className="inline-flex items-center gap-1 rounded-lg px-2 py-1 text-sm font-semibold text-amber-900 transition-colors hover:bg-amber-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-amber-600 disabled:cursor-default disabled:opacity-40"
              >
                Review
                <ArrowRight size={15} />
              </button>
            </div>
          </section>
        </div>

        <SearchToolbar>
          <SearchToolbar.Left>
            <Input
              fullWidth
              leftIcon={<Search size={18} />}
              placeholder="Search by name, mobile or email..."
              value={search}
              onChange={(event) =>
                setSearch(event.target.value)
              }
            />
          </SearchToolbar.Left>

          <SearchToolbar.Right>
            <div className="flex flex-wrap items-center gap-4">
              <div className="flex items-center gap-2">
                <label
                  htmlFor="customer-status-filter"
                  className="text-sm text-[var(--color-text-secondary)]"
                >
                  Status
                </label>

                <select
                  id="customer-status-filter"
                  value={statusFilter}
                  onChange={(event) =>
                    setStatusFilter(
                      event.target.value as TableStatusFilter
                    )
                  }
                  className="h-10 rounded-xl border border-[var(--color-border)] bg-[var(--color-background-secondary)] px-3 text-sm text-[var(--color-text)] outline-none focus:border-[var(--color-primary)]"
                >
                  <option value="ACTIVE">Active</option>
                  <option value="INACTIVE">Inactive</option>
                  <option value="ALL">
                    Active & Inactive
                  </option>
                </select>
              </div>

              <div className="flex items-center gap-2">
                <label
                  htmlFor="customer-rows-per-page"
                  className="text-sm text-[var(--color-text-secondary)]"
                >
                  Rows
                </label>

                <select
                  id="customer-rows-per-page"
                  value={rowsPerPage}
                  onChange={(event) =>
                    setRowsPerPage(
                      Number(event.target.value)
                    )
                  }
                  className="h-10 rounded-xl border border-[var(--color-border)] bg-[var(--color-background-secondary)] px-3 text-sm text-[var(--color-text)] outline-none focus:border-[var(--color-primary)]"
                >
                  <option value={10}>10</option>
                  <option value={20}>20</option>
                  <option value={50}>50</option>
                </select>
              </div>
            </div>
          </SearchToolbar.Right>
        </SearchToolbar>

        <p className="text-sm text-[var(--color-text-secondary)]">
          Showing{" "}
          <span className="font-semibold text-[var(--color-text)]">
            {filteredCustomers.length}
          </span>{" "}
          {statusFilter === "ACTIVE"
            ? "active "
            : statusFilter === "INACTIVE"
              ? "inactive "
              : ""}
          customer
          {filteredCustomers.length !== 1 ? "s" : ""}
        </p>

        <CustomerTable
          customers={paginatedCustomers}
          loading={loading}
          error={error}
          onEdit={handleManageCustomer}
          onApprove={(customer) =>
            openAction("approve", customer)
          }
          onReactivate={(customer) =>
            openAction("reactivate", customer)
          }
          onDeactivate={(customer) =>
            openAction("deactivate", customer)
          }
          actionsDisabled={loading || savingAction}
          currentPage={currentPage}
          totalPages={totalPages}
          onPrevious={() =>
            setCurrentPage((page) =>
              Math.max(1, page - 1)
            )
          }
          onNext={() =>
            setCurrentPage((page) =>
              Math.min(totalPages, page + 1)
            )
          }
        />
      </div>

      <Modal
        open={isPendingReviewOpen}
        title={`Pending Registrations (${pendingCustomers.length})`}
        onClose={() => setIsPendingReviewOpen(false)}
        footer={
          <Button
            variant="secondary"
            onClick={() => setIsPendingReviewOpen(false)}
          >
            Close
          </Button>
        }
      >
        {error ? (
          <div className="space-y-3 py-4 text-center">
            <p
              role="alert"
              className="text-sm text-red-500"
            >
              {error}
            </p>

            <Button
              variant="outline"
              disabled={loading}
              onClick={() => void fetchCustomers()}
            >
              Try Again
            </Button>
          </div>
        ) : loading ? (
          <p className="py-8 text-center text-sm text-[var(--color-text-secondary)]">
            Loading registrations...
          </p>
        ) : pendingCustomers.length === 0 ? (
          <div className="py-8 text-center">
            <UserCheck
              size={32}
              className="mx-auto text-[var(--color-primary)]"
            />

            <p className="mt-3 font-medium text-[var(--color-text)]">
              All caught up
            </p>

            <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
              No registrations are waiting for review.
            </p>
          </div>
        ) : (
          <>
            <p className="mb-3 text-sm text-[var(--color-text-secondary)]">
              Select an action to review the customer's
              contact details before confirming.
            </p>

            <ul className="max-h-[50vh] overflow-y-auto divide-y divide-[var(--color-border)]">
              {pendingCustomers.map((customer) => (
                <li
                  key={customer.customerId}
                  className="flex flex-wrap items-center gap-x-4 gap-y-2 py-3"
                >
                  <span className="min-w-0 flex-1 basis-36 break-words text-sm font-medium text-[var(--color-text)]">
                    {customer.fullName}
                  </span>

                  <div className="flex shrink-0 items-center gap-2">
                    <Button
                      size="sm"
                      disabled={savingAction}
                      aria-label={`Approve ${customer.fullName}`}
                      onClick={() =>
                        openAction("approve", customer)
                      }
                    >
                      <UserCheck size={15} />
                      Approve
                    </Button>

                    <Button
                      size="sm"
                      variant="outline"
                      disabled={savingAction}
                      aria-label={`Reject ${customer.fullName}`}
                      onClick={() =>
                        openAction("reject", customer)
                      }
                    >
                      <X size={15} />
                      Reject
                    </Button>
                  </div>
                </li>
              ))}
            </ul>
          </>
        )}
      </Modal>

      <CustomerRegistrationLinkModal
        open={isRegistrationOpen}
        onClose={() => setIsRegistrationOpen(false)}
      />

      <CustomerForm
        open={isManageOpen}
        selectedCustomer={selectedCustomer}
        onSuccess={handleSuccess}
        onCancel={handleCancel}
      />

      <Modal
        open={pendingAction !== null}
        title={actionDetails?.title ?? "Update Customer"}
        onClose={closeAction}
        footer={
          <>
            <Button
              variant="secondary"
              disabled={savingAction}
              onClick={closeAction}
            >
              {showRegistrationDetails
                ? "Back"
                : "Cancel"}
            </Button>

            <Button
              variant={
                isDestructiveAction
                  ? "danger"
                  : "primary"
              }
              disabled={savingAction || !pendingAction}
              onClick={() => void confirmAction()}
            >
              {savingAction
                ? actionDetails?.progress
                : actionDetails?.button}
            </Button>
          </>
        }
      >
        <p>
          {actionDetails?.title}{" "}
          <strong>
            {pendingAction?.customer.fullName}
          </strong>
          ?
        </p>

        {showRegistrationDetails && pendingAction && (
          <div className="mt-3 rounded-lg border border-[var(--color-border)] bg-[var(--color-background-secondary)] p-3">
            <p className="text-sm text-[var(--color-text)]">
              {pendingAction.customer.mobileNumber}
            </p>

            {pendingAction.customer.email && (
              <p className="mt-1 break-all text-sm text-[var(--color-text-secondary)]">
                {pendingAction.customer.email}
              </p>
            )}
          </div>
        )}

        <p className="mt-3 text-sm text-[var(--color-text-secondary)]">
          {actionDetails?.explanation}
        </p>

        {actionError && (
          <p
            role="alert"
            className="mt-3 text-sm text-red-500"
          >
            {actionError}
          </p>
        )}
      </Modal>
    </>
  );
}
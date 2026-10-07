import {
  useCallback,
  useEffect,
  useState,
} from "react";

import {
  Navigate,
  Outlet,
  useLocation,
} from "react-router-dom";

import AppShell from "@/components/common/layout/AppShell";
import Sidebar from "@/components/common/layout/Sidebar";
import Topbar from "@/components/common/layout/Topbar";
import Button from "@/components/common/ui/Button/Button";

import { customerNavigation } from "@/config/navigation";
import { ROUTES } from "@/constants/routes";

import { useCustomerLogout } from "@/features/auth/hooks";
import { getCustomer } from "@/features/auth/utils/auth.utils";

import { CustomerClosureNotice } from "@/features/customer/closures";

import NotificationBell from "@/features/customer/notifications/components/NotificationBell";

import { profileApi } from "@/features/customer/profile/api/profileApi";

type CustomerStatus = "ACTIVE" | "INACTIVE";

type StatusState = {
  customerId: number;
  status: CustomerStatus | null;
  error: string | null;
};

export default function CustomerLayout() {
  const { logout } = useCustomerLogout();
  const location = useLocation();

  const customer = getCustomer();
  const customerId = customer?.customerId;

  const messName =
    customer?.messName?.trim() || "Smart Mess";

  const [statusState, setStatusState] =
    useState<StatusState | null>(null);

  const [retryVersion, setRetryVersion] =
    useState(0);

  useEffect(() => {
    if (customerId === undefined) {
      return;
    }

    let cancelled = false;
    let fetching = false;

    async function fetchStatus() {
      if (fetching || customerId === undefined) {
        return;
      }

      fetching = true;

      try {
        const response =
          await profileApi.getProfile(customerId);

        if (!cancelled) {
          setStatusState({
            customerId,
            status: response.data.status,
            error: null,
          });
        }
      } catch (error) {
        if (!cancelled) {
          setStatusState((previous) => ({
            customerId,
            status:
              previous !== null &&
              previous.customerId === customerId
                ? previous.status
                : null,
            error:
              error instanceof Error
                ? error.message
                : "Could not check your account status.",
          }));
        }
      } finally {
        fetching = false;
      }
    }

    function handleFocus() {
      void fetchStatus();
    }

    function handleVisibilityChange() {
      if (document.visibilityState === "visible") {
        void fetchStatus();
      }
    }

    void fetchStatus();

    window.addEventListener("focus", handleFocus);

    document.addEventListener(
      "visibilitychange",
      handleVisibilityChange
    );

    return () => {
      cancelled = true;

      window.removeEventListener(
        "focus",
        handleFocus
      );

      document.removeEventListener(
        "visibilitychange",
        handleVisibilityChange
      );
    };
  }, [customerId, retryVersion, location.pathname]);

  const retryStatus = useCallback(() => {
    setRetryVersion((current) => current + 1);
  }, []);

  const currentStatus =
    statusState !== null &&
    statusState.customerId === customerId
      ? statusState.status
      : null;

  const statusError =
    statusState !== null &&
    statusState.customerId === customerId
      ? statusState.error
      : null;

  const inactive = currentStatus === "INACTIVE";

  const pathname =
    location.pathname.replace(/\/+$/, "");

  const viewingMenu =
    pathname === "/customer" ||
    pathname === "/customer/menu";

  const navigation = inactive
    ? customerNavigation.filter(
        (item) => item.path !== "/customer"
      )
    : customerNavigation;

  let content;

  if (customerId === undefined) {
    content = (
      <div className="space-y-4 p-6">
        <p>Your customer session could not be loaded.</p>

        <Button onClick={logout}>
          Return to Login
        </Button>
      </div>
    );
  } else if (viewingMenu && statusError) {
    content = (
      <div className="space-y-4 p-6">
        <p
          role="alert"
          className="text-sm text-red-500"
        >
          {statusError}
        </p>

        <Button
          variant="secondary"
          onClick={retryStatus}
        >
          Retry Account Check
        </Button>
      </div>
    );
  } else if (viewingMenu && currentStatus === null) {
    content = (
      <p className="p-6 text-sm text-[var(--color-text-secondary)]">
        Checking account status...
      </p>
    );
  } else if (viewingMenu && inactive) {
    content = (
      <Navigate
        to={ROUTES.MY_BILLS}
        replace
      />
    );
  } else {
    content = <Outlet />;
  }

  return (
    <AppShell
      sidebar={
        <Sidebar
          title="Smart Mess"
          subtitle="Meal Planning & Mess Operations"
          navigation={navigation}
          bottomContent={
            currentStatus === "ACTIVE"
              ? <CustomerClosureNotice />
              : undefined
          }
          account={{
            name: customer?.fullName ?? "Customer",
            profilePath: ROUTES.PROFILE,
            onLogout: logout,
          }}
        />
      }
      topbar={
        <Topbar
          title={messName}
          actions={<NotificationBell />}
        />
      }
    >
      {inactive && (
        <div className="mb-5 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
          <p className="text-sm font-medium">
            Your mess membership is inactive.
          </p>

          <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
            You can view your meal history, bills,
            mess details, and profile, and pay
            outstanding bills. Contact your mess
            owner to reactivate daily meal access.
          </p>
        </div>
      )}

      {statusError && !viewingMenu && (
        <div
          role="alert"
          className="mb-5 flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[var(--color-border)] p-4"
        >
          <p className="text-sm text-red-500">
            {statusError}
          </p>

          <Button
            variant="secondary"
            size="sm"
            onClick={retryStatus}
          >
            Retry Account Check
          </Button>
        </div>
      )}

      {content}
    </AppShell>
  );
}
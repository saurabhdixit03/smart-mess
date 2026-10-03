import { Outlet } from "react-router-dom";

import AppShell from "@/components/common/layout/AppShell";
import Sidebar from "@/components/common/layout/Sidebar";
import Topbar from "@/components/common/layout/Topbar";

import { customerNavigation } from "@/config/navigation";
import { ROUTES } from "@/constants/routes";

import { useCustomerLogout } from "@/features/auth/hooks";
import { getCustomer } from "@/features/auth/utils/auth.utils";

import { CustomerClosureNotice } from "@/features/customer/closures";

import NotificationBell from "@/features/customer/notifications/components/NotificationBell";
import { useNotifications } from "@/features/customer/notifications/hooks";

export default function CustomerLayout() {
  const { logout } = useCustomerLogout();

  const customer = getCustomer();
  const messName = customer?.messName?.trim() || "Smart Mess";

  useNotifications();

  return (
    <AppShell
      sidebar={
        <Sidebar
          title="Smart Mess"
          subtitle="Meal Planning & Mess Operations"
          navigation={customerNavigation}
          bottomContent={<CustomerClosureNotice />}
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
      <Outlet />
    </AppShell>
  );
}
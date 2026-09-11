import { Outlet } from "react-router-dom";

import AppShell from "@/components/common/layout/AppShell";
import Sidebar from "@/components/common/layout/Sidebar";
import Topbar from "@/components/common/layout/Topbar";

import { customerNavigation } from "@/config/navigation";

import { useCustomerLogout } from "@/features/auth/hooks";
import { getCustomer } from "@/features/auth/utils/auth.utils";

import { CustomerClosureNotice } from "@/features/customer/closures";
import NotificationBell from "@/features/customer/notifications/components/NotificationBell";
import { useNotifications } from "@/features/customer/notifications/hooks";

export default function CustomerLayout() {
  const { logout } = useCustomerLogout();

  const customer = getCustomer();

  /**
   * Initializes customer notifications.
   *
   * For now this establishes the REST +
   * WebSocket notification flow.
   * Visual notification UI will be added next.
   */
  useNotifications();

  return (
    <AppShell
      sidebar={
        <Sidebar
          title="Smart Mess"
          subtitle="Meal Planning & Mess Operations"
          navigation={customerNavigation}
          bottomContent={
            <CustomerClosureNotice />
          }
          account={{
            name: customer?.fullName ?? "Customer",
            role: "Customer",
            onLogout: logout,
          }}
        />
      }
      topbar={
        <Topbar
          title="Smart Mess"
          actions={<NotificationBell />}
        />
      }
    >
      <Outlet />
    </AppShell>
  );
}
import { Outlet } from "react-router-dom";

import AppShell from "@/components/common/layout/AppShell";
import Sidebar from "@/components/common/layout/Sidebar";
import Topbar from "@/components/common/layout/Topbar";

import { getCustomer } from "@/features/auth/utils/auth.utils";
import { useCustomerLogout } from "@/features/auth/hooks";

import { customerNavigation } from "@/config/navigation";

export default function CustomerLayout() {
  const { logout } = useCustomerLogout();

  const customer = getCustomer();

  return (
    <AppShell
      sidebar={
        <Sidebar
          title="Smart Mess"
          subtitle="Meal Planning & Mess Operations"
          navigation={customerNavigation}
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
        />
      }
    >
      <Outlet />
    </AppShell>
  );
}
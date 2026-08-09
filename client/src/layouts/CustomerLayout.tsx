import { Outlet } from "react-router-dom";

import AppShell from "@/components/common/layout/AppShell";
import Sidebar from "@/components/common/layout/Sidebar";
import Topbar from "@/components/common/layout/Topbar";

import { Button } from "@/components/common/ui";
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
          subtitle="Customer Portal"
          navigation={customerNavigation}
        />
      }
      topbar={
        <Topbar
          title={customer?.fullName ?? "Customer"}
          actions={
            <Button
              variant="outline"
              size="sm"
              onClick={logout}
            >
              Logout
            </Button>
          }
        />
      }
    >
      <Outlet />
    </AppShell>
  );
}
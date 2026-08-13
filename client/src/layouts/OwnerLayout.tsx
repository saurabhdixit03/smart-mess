import { Outlet } from "react-router-dom";

import AppShell from "@/components/common/layout/AppShell";
import Sidebar from "@/components/common/layout/Sidebar";
import Topbar from "@/components/common/layout/Topbar";

import { getOwner } from "@/features/auth/utils/auth.utils";
import { useOwnerLogout } from "@/features/auth/hooks";

import { ownerNavigation } from "@/config/navigation";

export default function OwnerLayout() {
  const { logout } = useOwnerLogout();

  const owner = getOwner();

  return (
    <AppShell
      sidebar={
        <Sidebar
          title="Smart Mess"
          subtitle="Meal Planning & Mess Operations"
          navigation={ownerNavigation}
          account={{
            name: owner?.fullName ?? "Mess Owner",
            role: "Mess Owner",
            onLogout: logout,
          }}
        />
      }
      topbar={
        <Topbar
          title={owner?.messName ?? "Smart Mess"}
        />
      }
    >
      <Outlet />
    </AppShell>
  );
}
import { Outlet } from "react-router-dom";

import AppShell from "@/components/common/layout/AppShell";
import Sidebar from "@/components/common/layout/Sidebar";
import Topbar from "@/components/common/layout/Topbar";

import { Button } from "@/components/common/ui";
import { getOwner } from "@/features/auth/utils/auth.utils";
import { useOwnerLogout } from "@/features/auth/hooks";

import { ownerNavigation } from "@/config/navigation";

const owner = getOwner();

export default function OwnerLayout() {
  const { logout } = useOwnerLogout();

  return (
    <AppShell
      sidebar={
        <Sidebar
          title="Smart Mess"
          subtitle="Owner Portal"
          navigation={ownerNavigation}
        />
      }
      topbar={
        <Topbar
  title={owner?.messName ?? "Smart Mess"}
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
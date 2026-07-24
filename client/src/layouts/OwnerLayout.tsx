import { Outlet } from "react-router-dom";

import AppShell from "@/components/common/layout/AppShell";
import Sidebar from "@/components/common/layout/Sidebar";
import Topbar from "@/components/common/layout/Topbar";

import { ownerNavigation } from "@/config/navigation";

export default function OwnerLayout() {
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
          title="Dashboard"
        />
      }
    >
      <Outlet />
    </AppShell>
  );
}
import { Outlet } from "react-router-dom";

import AppShell from "@/components/common/layout/AppShell";
import Sidebar from "@/components/common/layout/Sidebar";
import Topbar from "@/components/common/layout/Topbar";

import { customerNavigation } from "@/config/navigation";

export default function CustomerLayout() {
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
          title="Today's Menu"
        />
      }
    >
      <Outlet />
    </AppShell>
  );
}
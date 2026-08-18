import { useEffect, type ReactNode } from "react";
import { useLocation } from "react-router-dom";

import MobileDrawer from "../MobileNavigation/MobileDrawer";
import {
  MobileNavigationProvider,
  useMobileNavigation,
} from "../MobileNavigation/MobileNavigationContext";

type AppShellProps = {
  sidebar: ReactNode;
  topbar: ReactNode;
  children: ReactNode;
};

export default function AppShell({
  sidebar,
  topbar,
  children,
}: AppShellProps) {
  return (
    <MobileNavigationProvider>
      <AppShellContent
        sidebar={sidebar}
        topbar={topbar}
      >
        {children}
      </AppShellContent>
    </MobileNavigationProvider>
  );
}

function AppShellContent({
  sidebar,
  topbar,
  children,
}: AppShellProps) {
  const { close } = useMobileNavigation();
  const location = useLocation();

  // Close the mobile drawer whenever navigation occurs.
  useEffect(() => {
    close();
  }, [location.pathname, close]);

  return (
    <div className="flex h-screen bg-[var(--color-background)]">
      {/* Desktop Sidebar */}
      <aside className="hidden shrink-0 md:block">
        {sidebar}
      </aside>

      {/* Mobile Drawer */}
      <MobileDrawer>
        {sidebar}
      </MobileDrawer>

      {/* Main Application */}
      <div className="flex min-w-0 flex-1 flex-col">
        {/* Topbar */}
        <div className="shrink-0">
          {topbar}
        </div>

        {/* Page Content */}
        <main className="min-h-0 flex-1 overflow-y-auto p-4 sm:p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
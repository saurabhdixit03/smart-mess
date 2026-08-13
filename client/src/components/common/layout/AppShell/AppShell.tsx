import type { ReactNode } from "react";

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
    <div className="flex h-screen bg-[var(--color-background)]">
      {/* Sidebar */}
      <aside className="shrink-0">
        {sidebar}
      </aside>

      {/* Main Application */}
      <div className="flex min-w-0 flex-1 flex-col">
        {/* Topbar */}
        <div className="shrink-0">
          {topbar}
        </div>

        {/* Page Content */}
        <main className="min-h-0 flex-1 overflow-y-auto p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
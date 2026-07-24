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
    <div className="flex min-h-screen bg-[var(--color-background)]">

      {sidebar}

      <div className="flex min-w-0 flex-1 flex-col">

        {topbar}

        <main className="flex-1 overflow-auto p-6">
          {children}
        </main>

      </div>

    </div>
  );
}
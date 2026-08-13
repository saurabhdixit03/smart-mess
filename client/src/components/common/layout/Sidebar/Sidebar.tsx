import type { LucideIcon } from "lucide-react";

import SidebarItem from "./SidebarItem";

type NavigationItem = {
  label: string;
  path: string;
  icon: LucideIcon;
};

type SidebarAccount = {
  name: string;
  role: string;
  onLogout: () => void;
};

type SidebarProps = {
  title: string;
  subtitle: string;
  navigation: NavigationItem[];
  account: SidebarAccount;
};

export default function Sidebar({
  title,
  subtitle,
  navigation,
  account,
}: SidebarProps) {
  return (
    <aside
  className="
    flex
    h-screen
    w-72
    flex-col
    overflow-hidden
    border-r
    border-[var(--color-border)]
    bg-[var(--color-surface)]
  "

    >
      {/* Branding */}
      <div className="px-6 py-5">
        <h1 className="text-xl font-bold">
          {title}
        </h1>

        <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
          {subtitle}
        </p>
      </div>

      {/* Navigation */}
      <nav className="flex flex-1 flex-col gap-2 p-4">
        {navigation.map((item) => (
          <SidebarItem
            key={item.path}
            {...item}
          />
        ))}
      </nav>

      {/* Account */}
      <div className="border-t border-[var(--color-border)] p-4">
        <div className="flex items-center gap-3 rounded-xl px-3 py-3">
          <div
            className="
              flex
              h-10
              w-10
              shrink-0
              items-center
              justify-center
              rounded-full
              bg-[var(--color-primary)]
              text-sm
              font-semibold
              text-white
            "
          >
            {account.name.charAt(0).toUpperCase()}
          </div>

          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-semibold">
              {account.name}
            </p>

            <p className="mt-0.5 text-xs text-[var(--color-text-secondary)]">
              {account.role}
            </p>
          </div>

          <button
            type="button"
            onClick={account.onLogout}
            className="
              shrink-0
              rounded-lg
              px-2
              py-1.5
              text-xs
              font-medium
              text-[var(--color-text-secondary)]
              transition-colors
              hover:bg-[var(--color-surface-hover)]
              hover:text-[var(--color-danger)]
            "
          >
            Logout
          </button>
        </div>
      </div>
    </aside>
  );
}
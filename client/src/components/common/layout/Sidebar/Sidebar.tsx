import {
  useEffect,
  useRef,
  useState,
} from "react";

import {
  NavLink,
} from "react-router-dom";

import clsx from "clsx";

import {
  LogOut,
  MoreVertical,
  type LucideIcon,
} from "lucide-react";

import type {
  ReactNode,
} from "react";

import SidebarItem from "./SidebarItem";

type NavigationItem = {
  label: string;
  path: string;
  icon: LucideIcon;
};

type SidebarAccount = {
  name: string;
  role?: string;
  profilePath?: string;
  onLogout: () => void;
};

type SidebarProps = {
  title: string;
  subtitle: string;
  navigation: NavigationItem[];
  account: SidebarAccount;
  bottomContent?: ReactNode;
};

export default function Sidebar({
  title,
  subtitle,
  navigation,
  account,
  bottomContent,
}: SidebarProps) {
  const [
    accountMenuOpen,
    setAccountMenuOpen,
  ] = useState(false);

  const accountMenuRef =
    useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleOutsideClick(
      event: MouseEvent
    ) {
      if (
        accountMenuRef.current &&
        !accountMenuRef.current.contains(
          event.target as Node
        )
      ) {
        setAccountMenuOpen(false);
      }
    }

    function handleEscape(
      event: KeyboardEvent
    ) {
      if (event.key === "Escape") {
        setAccountMenuOpen(false);
      }
    }

    document.addEventListener(
      "mousedown",
      handleOutsideClick
    );

    document.addEventListener(
      "keydown",
      handleEscape
    );

    return () => {
      document.removeEventListener(
        "mousedown",
        handleOutsideClick
      );

      document.removeEventListener(
        "keydown",
        handleEscape
      );
    };
  }, []);

  function handleLogout() {
    setAccountMenuOpen(false);

    account.onLogout();
  }

  const accountDetails = (
    <>
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
        {account.name
          .charAt(0)
          .toUpperCase()}
      </div>

      <div className="min-w-0 flex-1">

        <p className="truncate text-sm font-semibold">
          {account.name}
        </p>

        {account.role && (
          <p className="mt-0.5 text-xs text-[var(--color-text-secondary)]">
            {account.role}
          </p>
        )}

      </div>
    </>
  );

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

      {/* Optional contextual content */}
      {bottomContent && (
        <div className="px-4 pb-4">
          {bottomContent}
        </div>
      )}

      {/* Account */}
      <div
        ref={accountMenuRef}
        className="
          relative
          border-t
          border-[var(--color-border)]
          p-4
        "
      >
        <div className="flex items-center gap-2">

          {account.profilePath ? (
            <NavLink
              to={account.profilePath}
              onClick={() =>
                setAccountMenuOpen(false)
              }
              className={({ isActive }) =>
                clsx(
                  "flex min-w-0 flex-1 items-center gap-3 rounded-xl px-3 py-3 transition-colors",
                  isActive
                    ? "bg-[var(--color-surface-hover)]"
                    : "hover:bg-[var(--color-surface-hover)]"
                )
              }
              aria-label={`Open ${account.name}'s profile`}
            >
              {accountDetails}
            </NavLink>
          ) : (
            <div className="flex min-w-0 flex-1 items-center gap-3 rounded-xl px-3 py-3">
              {accountDetails}
            </div>
          )}

          <button
            type="button"
            onClick={() =>
              setAccountMenuOpen(
                (current) => !current
              )
            }
            className="
              flex
              h-9
              w-9
              shrink-0
              items-center
              justify-center
              rounded-lg
              text-[var(--color-text-secondary)]
              transition-colors
              hover:bg-[var(--color-surface-hover)]
              hover:text-[var(--color-text)]
            "
            aria-label="Open account menu"
            aria-haspopup="menu"
            aria-expanded={accountMenuOpen}
          >
            <MoreVertical size={19} />
          </button>

        </div>

        {accountMenuOpen && (
          <div
            role="menu"
            className="
              absolute
              bottom-full
              right-4
              z-50
              mb-2
              w-44
              rounded-xl
              border
              border-[var(--color-border)]
              bg-[var(--color-surface)]
              p-1.5
              shadow-lg
            "
          >
            <button
              type="button"
              role="menuitem"
              onClick={handleLogout}
              className="
                flex
                w-full
                items-center
                gap-2
                rounded-lg
                px-3
                py-2.5
                text-left
                text-sm
                font-medium
                text-[var(--color-text-secondary)]
                transition-colors
                hover:bg-red-50
                hover:text-[var(--color-danger)]
              "
            >
              <LogOut size={17} />

              Logout
            </button>
          </div>
        )}

      </div>

    </aside>
  );
}
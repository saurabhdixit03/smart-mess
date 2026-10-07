import { Menu } from "lucide-react";
import type { ReactNode } from "react";

import {
  getAuthRole,
  getOwner,
} from "@/features/auth/utils/auth.utils";

import {
  useMobileNavigation,
} from "../MobileNavigation/useMobileNavigation";

type TopbarProps = {
  title?: string;
  actions?: ReactNode;
};

export default function Topbar({
  title,
  actions,
}: TopbarProps) {
  const { open } = useMobileNavigation();

  const messName =
    getAuthRole() === "OWNER"
      ? getOwner()?.messName?.trim()
      : undefined;

  const displayTitle = messName || title || "Smart Mess";

  return (
    <header
      className="
        flex
        h-16
        shrink-0
        items-center
        justify-between
        gap-3
        border-b
        border-[var(--color-border)]
        bg-[var(--color-surface)]
        px-4
        sm:px-6
      "
    >
      <div className="flex min-w-0 items-center gap-3">
        <button
          type="button"
          onClick={open}
          aria-label="Open navigation"
          className="
            flex
            h-10
            w-10
            shrink-0
            items-center
            justify-center
            rounded-[var(--radius-md)]
            text-[var(--color-text-secondary)]
            transition-colors
            hover:bg-[var(--color-surface-hover)]
            hover:text-[var(--color-text)]
            focus:outline-none
            focus:ring-2
            focus:ring-[var(--color-primary)]/20
            md:hidden
          "
        >
          <Menu size={22} />
        </button>

        <span
          title={displayTitle}
          className="truncate text-sm font-semibold text-[var(--color-text)]"
        >
          {displayTitle}
        </span>
      </div>

      {actions && (
        <div className="flex shrink-0 items-center gap-3">
          {actions}
        </div>
      )}
    </header>
  );
}
import { Menu } from "lucide-react";
import type { ReactNode } from "react";

import { useMobileNavigation } from "../MobileNavigation/MobileNavigationContext";

type TopbarProps = {
  title?: string;
  actions?: ReactNode;
};

export default function Topbar({
  title,
  actions,
}: TopbarProps) {
  const { open } = useMobileNavigation();

  return (
    <header
      className="
        flex
        h-16
        shrink-0
        items-center
        justify-between
        border-b
        border-[var(--color-border)]
        bg-[var(--color-surface)]
        px-4
        sm:px-6
      "
    >
      <div className="flex min-w-0 items-center gap-3">
        {/* Mobile Menu */}
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

        {title && (
          <span className="truncate text-sm font-medium text-[var(--color-text-secondary)]">
            {title}
          </span>
        )}
      </div>

      {actions && (
        <div className="flex shrink-0 items-center gap-3">
          {actions}
        </div>
      )}
    </header>
  );
}
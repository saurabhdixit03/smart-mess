import { X } from "lucide-react";
import type { ReactNode } from "react";

import { useMobileNavigation } from "./MobileNavigationContext";

type MobileDrawerProps = {
  children: ReactNode;
};

export default function MobileDrawer({
  children,
}: MobileDrawerProps) {
  const { isOpen, close } = useMobileNavigation();

  return (
    <div
      className={`fixed inset-0 z-50 md:hidden ${
        isOpen ? "visible" : "invisible"
      }`}
      aria-hidden={!isOpen}
    >
      {/* Backdrop */}
      <button
        type="button"
        aria-label="Close navigation"
        onClick={close}
        className={`
          absolute
          inset-0
          bg-black/40
          transition-opacity
          duration-200
          ${isOpen ? "opacity-100" : "opacity-0"}
        `}
      />

      {/* Drawer */}
      <aside
        className={`
          absolute
          left-0
          top-0
          flex
          h-full
          w-72
          max-w-[85vw]
          flex-col
          bg-[var(--color-surface)]
          shadow-[var(--shadow-md)]
          transition-transform
          duration-200
          ease-out
          ${isOpen ? "translate-x-0" : "-translate-x-full"}
        `}
        aria-label="Mobile navigation"
      >
        {/* Close button */}
        <div className="flex h-16 shrink-0 items-center justify-end border-b border-[var(--color-border)] px-4">
          <button
            type="button"
            onClick={close}
            aria-label="Close navigation"
            className="
              flex
              h-10
              w-10
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
            "
          >
            <X size={22} />
          </button>
        </div>

        {children}
      </aside>
    </div>
  );
}
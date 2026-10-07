import { useId, type ReactNode } from "react";
import clsx from "clsx";

type ModalProps = {
  open: boolean;
  title?: string;
  children: ReactNode;
  footer?: ReactNode;
  onClose: () => void;
  size?: "sm" | "md" | "lg" | "xl";
};

export default function Modal({
  open,
  title,
  children,
  footer,
  onClose,
  size = "md",
}: ModalProps) {
  const titleId = useId();

  if (!open) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? titleId : undefined}
        aria-label={title ? undefined : "Dialog"}
        className={clsx(
          "flex w-full min-w-0 flex-col",
          "max-h-[calc(100dvh-2rem)]",
          "overflow-hidden rounded-2xl",
          "border border-[var(--color-border)]",
          "bg-[var(--color-surface)] shadow-xl",
          {
            "max-w-sm": size === "sm",
            "max-w-md": size === "md",
            "max-w-lg": size === "lg",
            "max-w-xl": size === "xl",
          }
        )}
      >
        <header className="flex shrink-0 items-center justify-between gap-4 border-b border-[var(--color-border)] px-6 py-4">
          <h2
            id={titleId}
            className="min-w-0 break-words text-lg font-semibold"
          >
            {title}
          </h2>

          <button
            type="button"
            aria-label="Close dialog"
            onClick={onClose}
            className="shrink-0 rounded-md px-2 py-1 text-xl leading-none text-[var(--color-text-muted)] hover:text-[var(--color-text)]"
          >
            ×
          </button>
        </header>

        <div
          tabIndex={0}
          role="region"
          aria-label="Dialog content"
          className="
            min-h-0 min-w-0 flex-auto
            overflow-y-auto overscroll-contain
            p-4 sm:p-6
            [scrollbar-width:none]
            [&::-webkit-scrollbar]:hidden
            focus-visible:outline-none
            focus-visible:ring-2
            focus-visible:ring-inset
            focus-visible:ring-[var(--color-primary)]
          "
        >
          {children}
        </div>

        {footer && (
          <footer className="flex shrink-0 flex-wrap justify-end gap-3 border-t border-[var(--color-border)] px-6 py-4">
            {footer}
          </footer>
        )}
      </div>
    </div>
  );
}
import type { ReactNode } from "react";

type TopbarProps = {
  title?: string;
  actions?: ReactNode;
};

export default function Topbar({
  title,
  actions,
}: TopbarProps) {
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
        px-6
      "
    >
      <div>
        {title && (
          <span className="text-sm font-medium text-[var(--color-text-secondary)]">
            {title}
          </span>
        )}
      </div>

      {actions && (
        <div className="flex items-center gap-3">
          {actions}
        </div>
      )}
    </header>
  );
}
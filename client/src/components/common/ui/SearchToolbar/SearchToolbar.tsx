import type { ReactNode } from "react";
import clsx from "clsx";

type SearchToolbarProps = {
  children: ReactNode;
  className?: string;
};

export default function SearchToolbar({
  children,
  className,
}: SearchToolbarProps) {
  return (
    <div
      className={clsx(
        "interactive-surface",
        "rounded-2xl",
        "border border-[var(--color-border)]",
        "bg-[var(--color-surface)]",
        "shadow-sm",
        "px-5 py-4",
        className
      )}
    >
      <div
        className="
          flex
          flex-col
          gap-4

          lg:flex-row
          lg:items-center
          lg:justify-between
        "
      >
        {children}
      </div>
    </div>
  );
}

type SectionProps = {
  children: ReactNode;
  className?: string;
};

function Left({
  children,
  className,
}: SectionProps) {
  return (
    <div
      className={clsx(
        "flex flex-1 items-center gap-3",
        className
      )}
    >
      {children}
    </div>
  );
}

function Right({
  children,
  className,
}: SectionProps) {
  return (
    <div
      className={clsx(
        "flex items-center gap-3",
        className
      )}
    >
      {children}
    </div>
  );
}

SearchToolbar.Left = Left;
SearchToolbar.Right = Right;
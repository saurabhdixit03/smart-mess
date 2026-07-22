import type { ReactNode } from "react";

type PageHeaderProps = {
  title: string;
  description?: string;
  action?: ReactNode;
};

export default function PageHeader({
  title,
  description,
  action,
}: PageHeaderProps) {
  return (
    <header
      className="
        flex flex-col gap-5
        md:flex-row md:items-center md:justify-between
      "
    >
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight text-[var(--color-text)]">
          {title}
        </h1>

        {description && (
          <p className="max-w-2xl text-sm leading-6 text-[var(--color-text-secondary)]">
            {description}
          </p>
        )}
      </div>

      {action && (
        <div className="flex shrink-0">
          {action}
        </div>
      )}
    </header>
  );
}
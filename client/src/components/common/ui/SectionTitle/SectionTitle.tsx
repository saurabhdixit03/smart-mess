import type { ReactNode } from "react";

type SectionTitleProps = {
  title: string;
  description?: ReactNode;
};

export default function SectionTitle({
  title,
  description,
}: SectionTitleProps) {
  return (
    <div className="space-y-1">
      <h2 className="text-xl font-semibold">
        {title}
      </h2>

      {description && (
        <p className="text-sm text-[var(--color-text-secondary)]">
          {description}
        </p>
      )}
    </div>
  );
}
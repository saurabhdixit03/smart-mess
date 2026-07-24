import type { ReactNode } from "react";

type TopbarProps = {
  title: string;
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
        items-center
        justify-between
        border-b
        border-[var(--color-border)]
        bg-[var(--color-surface)]
        px-6
      "
    >
      <h1 className="text-xl font-semibold">
        {title}
      </h1>

      <div>{actions}</div>
    </header>
  );
}
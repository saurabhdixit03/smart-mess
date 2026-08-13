import { NavLink } from "react-router-dom";
import clsx from "clsx";
import type { LucideIcon } from "lucide-react";

type SidebarItemProps = {
  label: string;
  path: string;
  icon: LucideIcon;
};

export default function SidebarItem({
  label,
  path,
  icon: Icon,
}: SidebarItemProps) {
  return (
    <NavLink
      to={path}
      end
      className={({ isActive }) =>
        clsx(
          "flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition-all duration-200",
          isActive
            ? "bg-[var(--color-primary)] text-white"
            : "text-[var(--color-text)] hover:bg-[var(--color-surface-hover)]"
        )
      }
    >
      <Icon size={20} />

      <span>{label}</span>
    </NavLink>
  );
}
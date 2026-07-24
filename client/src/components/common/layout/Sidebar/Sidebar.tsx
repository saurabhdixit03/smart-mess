import type { LucideIcon } from "lucide-react";

import SidebarItem from "./SidebarItem";

type NavigationItem = {
  label: string;
  path: string;
  icon: LucideIcon;
};

type SidebarProps = {
  title: string;
  subtitle: string;
  navigation: NavigationItem[];
};

export default function Sidebar({
  title,
  subtitle,
  navigation,
}: SidebarProps){
  return (
    <aside
      className="
        flex
        w-72
        flex-col
        border-r
        border-[var(--color-border)]
        bg-[var(--color-surface)]
      "
    >
      <h1 className="text-xl font-bold">
  {title}
</h1>

<p className="mt-1 text-sm text-[var(--color-text-secondary)]">
  {subtitle}
</p>

      <nav className="flex flex-1 flex-col gap-2 p-4">
        {navigation.map((item) => (
          <SidebarItem
            key={item.path}
            {...item}
          />
        ))}
      </nav>
    </aside>
  );
}
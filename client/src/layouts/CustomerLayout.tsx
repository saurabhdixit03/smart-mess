import { Outlet } from "react-router-dom";

export default function CustomerLayout() {
  return (
    <main className="min-h-screen bg-[var(--color-background)]">
      <div className="mx-auto max-w-7xl p-8">
        <Outlet />
      </div>
    </main>
  );
}
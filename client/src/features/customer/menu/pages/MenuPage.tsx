import PageHeader from "@/components/common/ui/PageHeader";

import TodayMenus from "../components/TodayMenus";
import { MENU_PAGE } from "../constants";
import { useMenus } from "../hooks";

export default function MenuPage() {
  const {
    todayMenus,
    loading,
    error,
    fetchTodayMenus,
  } = useMenus();

  return (
    <section className="min-w-0 space-y-4">
      <PageHeader
        title={MENU_PAGE.TITLE}
        description={MENU_PAGE.DESCRIPTION}
      />

      <TodayMenus
        todayMenus={todayMenus}
        loading={loading}
        error={error}
        onRefresh={fetchTodayMenus}
      />
    </section>
  );
}
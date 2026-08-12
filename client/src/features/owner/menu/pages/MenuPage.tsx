import PageHeader from "@/components/common/ui/PageHeader";

import TodayMenus from "../components/TodayMenus";
import MenuHistoryTable from "../components/MenuHistoryTable/MenuHistoryTable";

import { useMenus } from "../hooks/useMenus";

export default function MenuPage() {

  const {
    todayMenus,
    loading,
    error,
    fetchTodayMenus,
  } = useMenus();

  return (

    <div className="space-y-8">

      <PageHeader
        title="Menus"
        description="Publish lunch and dinner menus."
      />

      <TodayMenus
        todayMenus={todayMenus}
        loading={loading}
        error={error}
        onRefresh={fetchTodayMenus}
      />

      <MenuHistoryTable />

    </div>

  );
}
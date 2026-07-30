import { BrowserRouter, Routes, Route } from "react-router-dom";

import OwnerLayout from "@/layouts/OwnerLayout";
import CustomerLayout from "@/layouts/CustomerLayout";

import NotFoundPage from "@/pages/NotFoundPage";
//import DesignSystemPage from "@/pages/DesignSystemPage";

import { CustomerPage } from "@/features/customers/pages";

import { MenuPage } from "@/features/menu";

import { DashboardPage } from "@/features/dashboard/pages";

import { MealRecordPage } from "@/features/meal-records/pages";

import { BillingPage } from "@/features/billing/pages";

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>

        <Route path="/owner" element={<OwnerLayout />}>
  <Route index element={<DashboardPage />} />

  <Route
    path="customers"
    element={<CustomerPage />}
  />

  {/* Next modules */}

  <Route
    path="menu"
    element={<MenuPage />}
/>

    <Route
  path="dashboard"
  element={<DashboardPage />}
/>

  <Route
    path="meals"
    element={<DashboardPage />}
  />

  <Route
  path="meal-records"
  element={<MealRecordPage />}
/>

  <Route
  path="billing"
  element={<BillingPage />}
/>

  <Route
    path="reports"
    element={<DashboardPage />}
  />

  <Route
    path="settings"
    element={<DashboardPage />}
  />
</Route>

        <Route path="/customer" element={<CustomerLayout />}>
          <Route index element={<CustomerPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />

      </Routes>
    </BrowserRouter>
  );
}
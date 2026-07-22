import { BrowserRouter, Routes, Route } from "react-router-dom";

import OwnerLayout from "@/layouts/OwnerLayout";
import CustomerLayout from "@/layouts/CustomerLayout";

import NotFoundPage from "@/pages/NotFoundPage";
import DesignSystemPage from "@/pages/DesignSystemPage";

import { CustomerPage } from "@/features/customers/pages";

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>

        <Route path="/owner" element={<OwnerLayout />}>
          <Route index element={<DesignSystemPage />} />
        </Route>

        <Route path="/customer" element={<CustomerLayout />}>
          <Route index element={<CustomerPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />

      </Routes>
    </BrowserRouter>
  );
}
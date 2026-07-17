import { BrowserRouter, Routes, Route } from "react-router-dom";

import OwnerLayout from "@/layouts/OwnerLayout";
import CustomerLayout from "@/layouts/CustomerLayout";
import NotFoundPage from "@/pages/NotFoundPage";

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/owner" element={<OwnerLayout />}>
            <Route index element={<div>Owner Dashboard</div>} />
        </Route>

        <Route path="/customer" element={<CustomerLayout />}>
            <Route index element={<div>Customer Home</div>} />
        </Route>
        
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}
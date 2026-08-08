import { Navigate, Outlet } from "react-router-dom";

import { isAuthenticated } from "@/features/auth/utils/auth.utils";

export default function PublicRoute() {
  if (isAuthenticated()) {
    return <Navigate to="/owner" replace />;
  }

  return <Outlet />;
}
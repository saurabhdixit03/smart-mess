import { Navigate, Outlet, useLocation } from "react-router-dom";

import { isAuthenticated } from "@/features/auth/utils/auth.utils";

export default function ProtectedRoute() {
  const location = useLocation();

  if (!isAuthenticated()) {
    return (
      <Navigate
        to="/owner/login"
        replace
        state={{ from: location }}
      />
    );
  }

  return <Outlet />;
}
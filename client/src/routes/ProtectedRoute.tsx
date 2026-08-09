import { Navigate, Outlet, useLocation } from "react-router-dom";

import {
  getAuthRole,
  isAuthenticated,
  type AuthRole,
} from "@/features/auth/utils/auth.utils";

type ProtectedRouteProps = {
  role: AuthRole;
  loginPath: string;
};

export default function ProtectedRoute({
  role,
  loginPath,
}: ProtectedRouteProps) {
  const location = useLocation();

  if (!isAuthenticated()) {
    return (
      <Navigate
        to={loginPath}
        replace
        state={{ from: location }}
      />
    );
  }

  if (getAuthRole() !== role) {
    return (
      <Navigate
        to={loginPath}
        replace
      />
    );
  }

  return <Outlet />;
}
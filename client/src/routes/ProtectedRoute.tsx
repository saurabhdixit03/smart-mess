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

  const authenticated = isAuthenticated();
  const currentRole = getAuthRole();

  // Not logged in
  if (!authenticated) {
    return (
      <Navigate
        to={loginPath}
        replace
        state={{ from: location }}
      />
    );
  }

  // Logged in but trying to access another portal
  if (currentRole !== role) {
    return (
      <Navigate
        to={loginPath}
        replace
      />
    );
  }

  return <Outlet />;
}
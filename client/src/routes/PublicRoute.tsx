import { Navigate, Outlet } from "react-router-dom";

import {
  getAuthRole,
  isAuthenticated,
  type AuthRole,
} from "@/features/auth/utils/auth.utils";

type PublicRouteProps = {
  role: AuthRole;
  redirectPath: string;
};

export default function PublicRoute({
  role,
  redirectPath,
}: PublicRouteProps) {
  if (isAuthenticated()) {
    if (getAuthRole() === role) {
      return (
        <Navigate
          to={redirectPath}
          replace
        />
      );
    }
  }

  return <Outlet />;
}
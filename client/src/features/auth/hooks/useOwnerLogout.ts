import { useNavigate } from "react-router-dom";

import { clearAuthSession } from "../utils/auth.utils";

export function useOwnerLogout() {
  const navigate = useNavigate();

  const logout = () => {
    clearAuthSession();

    navigate("/owner/login", {
      replace: true,
    });
  };

  return {
    logout,
  };
}
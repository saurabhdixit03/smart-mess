import { useNavigate } from "react-router-dom";

import { clearAuthSession } from "../utils/auth.utils";

import { toast } from "sonner";

export function useCustomerLogout() {
  const navigate = useNavigate();

  const logout = () => {
    clearAuthSession();

    toast.success("Loged out successfully.");


    navigate("/customer/login", {
      replace: true,
    });
  };

  return {
    logout,
  };
}
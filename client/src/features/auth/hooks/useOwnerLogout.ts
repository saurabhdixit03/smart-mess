import { useNavigate } from "react-router-dom";
import { toast } from "sonner";

import {
  clearAuthSession,
} from "../utils/auth.utils";

import {
  disconnectWebSocket,
} from "@/services/websocket/websocket.service";

export function useOwnerLogout() {
  const navigate = useNavigate();

  const logout = () => {
    disconnectWebSocket();
    clearAuthSession();

    toast.success(
      "Logged out successfully."
    );

    navigate("/owner/login", {
      replace: true,
    });
  };

  return {
    logout,
  };
}
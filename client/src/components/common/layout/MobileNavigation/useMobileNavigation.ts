import {
  createContext,
  useContext,
} from "react";

type MobileNavigationContextValue = {
  isOpen: boolean;
  open: () => void;
  close: () => void;
  toggle: () => void;
};

export const MobileNavigationContext =
  createContext<MobileNavigationContextValue | null>(
    null
  );

export function useMobileNavigation() {
  const context = useContext(
    MobileNavigationContext
  );

  if (!context) {
    throw new Error(
      "useMobileNavigation must be used inside MobileNavigationProvider"
    );
  }

  return context;
}
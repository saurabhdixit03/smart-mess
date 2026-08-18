import {
  createContext,
  useContext,
  useState,
  type ReactNode,
} from "react";

type MobileNavigationContextValue = {
  isOpen: boolean;
  open: () => void;
  close: () => void;
  toggle: () => void;
};

const MobileNavigationContext =
  createContext<MobileNavigationContextValue | null>(null);

type MobileNavigationProviderProps = {
  children: ReactNode;
};

export function MobileNavigationProvider({
  children,
}: MobileNavigationProviderProps) {
  const [isOpen, setIsOpen] = useState(false);

  const open = () => setIsOpen(true);
  const close = () => setIsOpen(false);
  const toggle = () => setIsOpen((previous) => !previous);

  return (
    <MobileNavigationContext.Provider
      value={{
        isOpen,
        open,
        close,
        toggle,
      }}
    >
      {children}
    </MobileNavigationContext.Provider>
  );
}

export function useMobileNavigation() {
  const context = useContext(MobileNavigationContext);

  if (!context) {
    throw new Error(
      "useMobileNavigation must be used inside MobileNavigationProvider"
    );
  }

  return context;
}
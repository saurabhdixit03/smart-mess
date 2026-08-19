import {
  createContext,
  useCallback,
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

  const open = useCallback(() => {
    setIsOpen(true);
  }, []);

  const close = useCallback(() => {
    setIsOpen(false);
  }, []);

  const toggle = useCallback(() => {
    setIsOpen((previous) => !previous);
  }, []);

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
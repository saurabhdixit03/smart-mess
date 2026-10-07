import {
  useCallback,
  useState,
  type ReactNode,
} from "react";

import {
  MobileNavigationContext,
} from "./useMobileNavigation";

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
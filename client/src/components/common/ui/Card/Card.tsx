import type { ReactNode } from "react";
import clsx from "clsx";

type CardProps = {
  children: ReactNode;
  className?: string;
};

type SectionProps = {
  children: ReactNode;
  className?: string;
};

function Header({ children, className }: SectionProps) {
  return (
    <div
      className={clsx(
        "border-b border-[var(--color-border)] px-6 py-4",
        className
      )}
    >
      {children}
    </div>
  );
}

function Body({ children, className }: SectionProps) {
  return (
    <div
      className={clsx(
        "px-6 py-5",
        className
      )}
    >
      {children}
    </div>
  );
}

function Footer({ children, className }: SectionProps) {
  return (
    <div
      className={clsx(
        "border-t border-[var(--color-border)] px-6 py-4",
        className
      )}
    >
      {children}
    </div>
  );
}

export default function Card({
  children,
  className,
}: CardProps) {
  return (
    <div
      className={clsx(
        "rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] shadow-sm",
        className
      )}
    >
      {children}
    </div>
  );
}

Card.Header = Header;
Card.Body = Body;
Card.Footer = Footer;
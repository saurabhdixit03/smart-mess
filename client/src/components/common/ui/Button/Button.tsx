import type { ButtonHTMLAttributes, ReactNode } from "react";
import clsx from "clsx";

type ButtonVariant =
  | "primary"
  | "secondary"
  | "outline"
  | "danger";

type ButtonSize =
  | "sm"
  | "md"
  | "lg";

interface ButtonProps
  extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode;
  variant?: ButtonVariant;
  size?: ButtonSize;
  fullWidth?: boolean;
}

const variantClasses: Record<ButtonVariant, string> = {
  primary: [
    "bg-[var(--color-primary)]",
    "text-white",
    "hover:bg-[var(--color-primary-hover)]",
    "shadow-[var(--shadow-sm)]",
    "hover:shadow-[var(--shadow-md)]",
  ].join(" "),

  secondary: [
    "bg-[var(--color-surface)]",
    "text-[var(--color-text)]",
    "border",
    "border-[var(--color-border)]",
    "hover:bg-[#F3F1EB]",
  ].join(" "),

  outline: [
    "bg-transparent",
    "text-[var(--color-primary)]",
    "border",
    "border-[var(--color-primary)]",
    "hover:bg-[#EEF3EE]",
  ].join(" "),

  danger: [
    "bg-[var(--color-danger)]",
    "text-white",
    "hover:brightness-95",
  ].join(" "),
};

const sizeClasses: Record<ButtonSize, string> = {
  sm: "h-9 px-4 text-sm",

  md: "h-11 px-5 text-sm",

  lg: "h-12 px-6 text-base",
};

export default function Button({
  children,
  variant = "primary",
  size = "md",
  fullWidth = false,
  className,
  ...props
}: ButtonProps) {
  return (
    <button
      className={clsx(
        "inline-flex items-center justify-center",
        "font-semibold",
        "rounded-[var(--radius-md)]",
        "transition-all duration-200 ease-out",
        "select-none",
        "cursor-pointer",
        "hover:-translate-y-0.5",
        "active:translate-y-0",
        "active:scale-[0.97]",

        "focus:outline-none",
        "focus:ring-2",
        "focus:ring-[var(--color-primary)]/20",

        "disabled:pointer-events-none",
        "disabled:opacity-50",

        variantClasses[variant],
        sizeClasses[size],

        fullWidth && "w-full",

        className
      )}
      {...props}
    >
      {children}
    </button>
  );
}
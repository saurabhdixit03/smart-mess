import { forwardRef } from "react";
import clsx from "clsx";

type SelectProps = Omit<
  React.SelectHTMLAttributes<HTMLSelectElement>,
  "size"
> & {
  inputSize?: "sm" | "md" | "lg";
  fullWidth?: boolean;
  error?: boolean;
};

const Select = forwardRef<HTMLSelectElement, SelectProps>(
  (
    {
      inputSize = "md",
      fullWidth = false,
      error = false,
      className,
      children,
      ...props
    },
    ref
  ) => {
    return (
      <select
        ref={ref}
        className={clsx(
          "rounded-xl border transition-all duration-200 outline-none",

          "bg-[var(--color-surface)]",
          "border-[var(--color-border)]",
          "text-[var(--color-text)]",

          "focus:border-[var(--color-primary)]",
          "focus:ring-2",
          "focus:ring-[var(--color-primary)]/20",

          {
            "h-9 px-3 text-sm": inputSize === "sm",
            "h-11 px-4 text-base": inputSize === "md",
            "h-13 px-5 text-lg": inputSize === "lg",

            "w-full": fullWidth,

            "border-red-500 focus:border-red-500 focus:ring-red-200":
              error,

            "opacity-60 cursor-not-allowed": props.disabled,
          },

          className
        )}
        {...props}
      >
        {children}
      </select>
    );
  }
);

Select.displayName = "Select";

export default Select;
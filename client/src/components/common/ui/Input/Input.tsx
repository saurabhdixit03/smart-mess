import { forwardRef } from "react";
import type { ReactNode } from "react";
import clsx from "clsx";

type InputProps = Omit<
  React.InputHTMLAttributes<HTMLInputElement>,
  "size"
> & {
  inputSize?: "sm" | "md" | "lg";
  fullWidth?: boolean;
  error?: boolean;

  leftIcon?: ReactNode;
  rightIcon?: ReactNode;
};

const Input = forwardRef<HTMLInputElement, InputProps>(
  (
    {
      inputSize = "md",
      fullWidth = false,
      error = false,

      leftIcon,
      rightIcon,

      className,
      ...props
    },
    ref
  ) => {
    return (
      <div className={clsx("relative", fullWidth && "w-full")}>

        {leftIcon && (
          <div
            className="
              pointer-events-none
              absolute
              left-4
              top-1/2
              -translate-y-1/2
              text-[var(--color-text-secondary)]
            "
          >
            {leftIcon}
          </div>
        )}

        <input
          ref={ref}
          className={clsx(
            "rounded-xl border transition-all duration-200 outline-none",

            "bg-[var(--color-surface)]",

            "border-[var(--color-border)]",

            "text-[var(--color-text)]",

            "placeholder:text-[var(--color-text-secondary)]",

            "focus:border-[var(--color-primary)]",
            "focus:ring-2",
            "focus:ring-[var(--color-primary)]/20",

            {
              "h-9 px-3 text-sm": inputSize === "sm",

              "h-11 px-4 text-base": inputSize === "md",

              "h-13 px-5 text-lg": inputSize === "lg",

              "w-full": fullWidth,

              "pl-11": !!leftIcon,

              "pr-11": !!rightIcon,

              "border-red-500 focus:border-red-500 focus:ring-red-200":
                error,

              "opacity-60 cursor-not-allowed":
                props.disabled,
            },

            className
          )}
          {...props}
        />

        {rightIcon && (
          <div
            className="
              absolute
              right-4
              top-1/2
              -translate-y-1/2
              text-[var(--color-text-secondary)]
            "
          >
            {rightIcon}
          </div>
        )}

      </div>
    );
  }
);

Input.displayName = "Input";

export default Input;
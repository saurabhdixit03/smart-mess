import { forwardRef } from "react";
import clsx from "clsx";

type TextareaProps = Omit<
  React.TextareaHTMLAttributes<HTMLTextAreaElement>,
  "size"
> & {
  size?: "sm" | "md" | "lg";
  fullWidth?: boolean;
  error?: boolean;
};

const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(
  (
    {
      size = "md",
      fullWidth = false,
      error = false,
      className,
      rows = 4,
      ...props
    },
    ref
  ) => {
    return (
      <textarea
        ref={ref}
        rows={rows}
        className={clsx(
          "rounded-xl border transition-all duration-200 outline-none resize-y",

          "bg-[var(--color-surface)]",
          "border-[var(--color-border)]",
          "text-[var(--color-text)]",
          "placeholder:text-[var(--color-text-muted)]",

          "focus:border-[var(--color-primary)]",
          "focus:ring-2",
          "focus:ring-[var(--color-primary)]/20",

          {
            "px-3 py-2 text-sm": size === "sm",
            "px-4 py-3 text-base": size === "md",
            "px-5 py-4 text-lg": size === "lg",

            "w-full": fullWidth,

            "border-red-500 focus:border-red-500 focus:ring-red-200":
              error,

            "opacity-60 cursor-not-allowed":
              props.disabled,
          },

          className
        )}
        {...props}
      />
    );
  }
);

Textarea.displayName = "Textarea";

export default Textarea;
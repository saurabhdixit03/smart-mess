import clsx from "clsx";

type BadgeVariant =
  | "primary"
  | "success"
  | "warning"
  | "danger"
  | "neutral";

type BadgeProps = {
  children: React.ReactNode;
  variant?: BadgeVariant;
  className?: string;
};

export default function Badge({
  children,
  variant = "primary",
  className,
}: BadgeProps) {
  return (
    <span
      className={clsx(
        "inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold",

        {
          "bg-[var(--color-primary)] text-white":
            variant === "primary",

          "bg-green-100 text-green-700":
            variant === "success",

          "bg-yellow-100 text-yellow-700":
            variant === "warning",

          "bg-red-100 text-red-700":
            variant === "danger",

          "bg-gray-100 text-gray-700":
            variant === "neutral",
        },

        className
      )}
    >
      {children}
    </span>
  );
}
import clsx from "clsx";

type StatusVariant =
  | "success"
  | "danger"
  | "warning"
  | "info";

type StatusBadgeProps = {
  label: string;
  variant?: StatusVariant;
};

const variantClasses: Record<StatusVariant, string> = {
  success: "bg-green-100 text-green-700",

  danger: "bg-red-100 text-red-700",

  warning: "bg-yellow-100 text-yellow-700",

  info: "bg-blue-100 text-blue-700",
};

export default function StatusBadge({
  label,
  variant = "info",
}: StatusBadgeProps) {
  return (
    <span
      className={clsx(
        "inline-flex items-center gap-2",
        "rounded-full",
        "px-3 py-1.5",
        "text-xs font-semibold tracking-wide",
        variantClasses[variant]
      )}
    >
      <span className="h-2 w-2 rounded-full bg-current opacity-80" />

      {label}
    </span>
  );
}
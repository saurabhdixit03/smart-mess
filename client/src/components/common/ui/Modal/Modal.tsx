import clsx from "clsx";

type ModalProps = {
  open: boolean;
  title?: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
  onClose: () => void;

  size?: "sm" | "md" | "lg";
};

export default function Modal({
  open,
  title,
  children,
  footer,
  onClose,
  size = "md",
}: ModalProps) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">

      <div
        className={clsx(
  "w-full rounded-2xl",
  {
    "max-w-sm": size === "sm",
    "max-w-md": size === "md",
    "max-w-lg": size === "lg",
  },
  "border border-[var(--color-border)]",
  "bg-[var(--color-surface)]",
  "shadow-xl"
)}
      >

        <div className="flex items-center justify-between border-b border-[var(--color-border)] px-6 py-4">

          <h2 className="text-lg font-semibold">
            {title}
          </h2>

          <button
            onClick={onClose}
            className="text-xl leading-none text-[var(--color-text-muted)] hover:text-[var(--color-text)]"
          >
            ×
          </button>

        </div>

        <div className="p-6">
          {children}
        </div>

        {footer && (
          <div className="flex justify-end gap-3 border-t border-[var(--color-border)] px-6 py-4">
            {footer}
          </div>
        )}

      </div>

    </div>
  );
}
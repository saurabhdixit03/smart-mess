import type { ReactNode } from "react";
import clsx from "clsx";

export type Column<T> = {
  key: keyof T | string;
  header: string;

  width?: string;

  headerClassName?: string;
  className?: string;

  render?: (row: T) => ReactNode;
};

type DataTableProps<T> = {
  columns: readonly Column<T>[];

  data: T[];

  rowKey: (row: T) => React.Key;

  rowClassName?: (row: T) => string;

  className?: string;
};

export default function DataTable<T>({
  columns,
  data,
  rowKey,
  rowClassName,
  className,
}: DataTableProps<T>) {
  return (
    <div
      className={clsx(
        "overflow-hidden",
        "rounded-2xl",
        "border border-[var(--color-border)]",
        "bg-[var(--color-surface)]",
        "shadow-sm",
        className
      )}
    >
      <table className="min-w-full border-separate border-spacing-0">
        <thead className="bg-[#FAFAF8]">
          <tr>
            {columns.map((column) => (
              <th
                key={String(column.key)}
                style={{
                  width: column.width,
                }}
                className={clsx(
                  `
                    border-b
                    border-[var(--color-border)]
                    px-6
                    py-4
                    text-left
                    text-xs
                    font-semibold
                    uppercase
                    tracking-wider
                    text-[var(--color-text-secondary)]
                  `,
                  column.headerClassName
                )}
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>

        <tbody>
          {data.length === 0 ? (
            <tr>
              <td
                colSpan={columns.length}
                className="px-6 py-12 text-center text-sm text-[var(--color-text-secondary)]"
              >
                No records found.
              </td>
            </tr>
          ) : (
            data.map((row) => (
              <tr
                key={rowKey(row)}
                className={clsx(
                  "transition-all duration-200 hover:bg-[#FAFAF8]",
                  rowClassName?.(row)
                )}
              >
                {columns.map((column) => (
                  <td
                    key={String(column.key)}
                    style={{
                      width: column.width,
                    }}
                    className={clsx(
                      `
                        border-b
                        border-[var(--color-border)]
                        px-6
                        py-5
                        text-sm
                        text-[var(--color-text)]
                      `,
                      column.className
                    )}
                  >
                    {column.render
                      ? column.render(row)
                      : String(
                          (row as Record<string, unknown>)[
                            column.key as string
                          ] ?? ""
                        )}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
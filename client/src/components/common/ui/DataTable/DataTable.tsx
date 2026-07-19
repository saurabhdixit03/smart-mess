import clsx from "clsx";

type Column<T> = {
  key: keyof T;
  header: string;
};

type DataTableProps<T> = {
  columns: readonly Column<T>[];
  data: T[];
  className?: string;
};

export default function DataTable<T extends Record<string, unknown>>({
  columns,
  data,
  className,
}: DataTableProps<T>) {
  return (
    <div
      className={clsx(
        "overflow-hidden rounded-2xl border border-[var(--color-border)]",
        className
      )}
    >
      <table className="min-w-full border-collapse">

        <thead className="bg-[var(--color-surface-muted)]">
          <tr>
            {columns.map((column) => (
              <th
                key={String(column.key)}
                className="border-b border-[var(--color-border)] px-4 py-3 text-left text-sm font-semibold text-[var(--color-text)]"
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>

        <tbody>
          {data.map((row, index) => (
            <tr
              key={index}
              className="transition-colors hover:bg-[var(--color-surface-hover)]"
            >
              {columns.map((column) => (
                <td
                  key={String(column.key)}
                  className="border-b border-[var(--color-border)] px-4 py-3 text-sm text-[var(--color-text)]"
                >
                  {String(row[column.key])}
                </td>
              ))}
            </tr>
          ))}
        </tbody>

      </table>
    </div>
  );
}
import Button from "../Button/Button";

type PaginationProps = {
  currentPage: number;
  totalPages: number;

  onPrevious: () => void;
  onNext: () => void;
};

export default function Pagination({
  currentPage,
  totalPages,
  onPrevious,
  onNext,
}: PaginationProps) {
  return (
    <div
      className="
        flex
        flex-col
        gap-4
        border-t
        border-[var(--color-border)]
        pt-5

        md:flex-row
        md:items-center
        md:justify-between
      "
    >
      <Button
        variant="secondary"
        onClick={onPrevious}
        disabled={currentPage === 1}
      >
        Previous
      </Button>

      <p className="text-sm font-medium text-[var(--color-text-secondary)]">
        Page <strong>{currentPage}</strong> of{" "}
        <strong>{totalPages}</strong>
      </p>

      <Button
        variant="secondary"
        onClick={onNext}
        disabled={currentPage === totalPages}
      >
        Next
      </Button>
    </div>
  );
}
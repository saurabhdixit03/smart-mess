import {
  useCallback,
  useEffect,
  useRef,
  useState,
  type FormEvent,
} from "react";

import Card from "@/components/common/ui/Card/Card";
import Button from "@/components/common/ui/Button/Button";
import Input from "@/components/common/ui/Input/Input";
import Select from "@/components/common/ui/Select/Select";

import { customerApi } from "@/features/owner/customers/api/customer.api";

import type {
  CustomerResponse,
} from "@/features/owner/customers/types/customer.types";

import type { GenerateBillRequest } from "../types";

type GenerationFilters = Omit<
  GenerateBillRequest,
  "billingMonth" | "billingYear"
>;

type GenerateBillCardProps = {
  billingMonth: number;
  billingYear: number;

  onMonthChange: (month: number) => void;
  onYearChange: (year: number) => void;

  onGenerate: (
    filters: GenerationFilters
  ) => Promise<boolean>;

  loading: boolean;
};

const MONTH_OPTIONS = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
];

const CURRENT_YEAR = new Date().getFullYear();

const YEAR_OPTIONS = Array.from(
  { length: 5 },
  (_, index) => CURRENT_YEAR - 2 + index
);

export default function GenerateBillCard({
  billingMonth,
  billingYear,
  onMonthChange,
  onYearChange,
  onGenerate,
  loading,
}: GenerateBillCardProps) {
  const [customers, setCustomers] =
    useState<CustomerResponse[]>([]);

  const [customersLoading, setCustomersLoading] =
    useState(true);

  const [customersError, setCustomersError] =
    useState<string | null>(null);

  const [customerId, setCustomerId] =
    useState("");

  const [startDate, setStartDate] =
    useState("");

  const [endDate, setEndDate] =
    useState("");

  const [validationError, setValidationError] =
    useState<string | null>(null);

  const period = `${billingYear}-${billingMonth}`;

  const [previousPeriod, setPreviousPeriod] =
    useState(period);

  const customerRequestRef = useRef(0);
  const submittingRef = useRef(false);

  const monthPrefix =
    `${billingYear}-${String(billingMonth).padStart(2, "0")}`;

  const firstDay = `${monthPrefix}-01`;

  const lastDayNumber = new Date(
    billingYear,
    billingMonth,
    0
  ).getDate();

  const lastDay =
    `${monthPrefix}-${String(lastDayNumber).padStart(2, "0")}`;

  const loadCustomers = useCallback((): Promise<void> => {
    const requestVersion =
      ++customerRequestRef.current;

    return customerApi
      .getAllCustomers()
      .then((response) => {
        if (
          requestVersion !== customerRequestRef.current
        ) {
          return;
        }

        setCustomers(
          response.data
            .filter(
              (customer) =>
                customer.status === "ACTIVE" ||
                customer.status === "INACTIVE"
            )
            .sort(
              (a, b) =>
                a.fullName.localeCompare(b.fullName)
            )
        );

        setCustomersError(null);
      })
      .catch((error: unknown) => {
        if (
          requestVersion === customerRequestRef.current
        ) {
          setCustomersError(
            error instanceof Error
              ? error.message
              : "Failed to load customers."
          );
        }
      })
      .finally(() => {
        if (
          requestVersion === customerRequestRef.current
        ) {
          setCustomersLoading(false);
        }
      });
  }, []);

  const fetchCustomers = useCallback((): Promise<void> => {
    setCustomersLoading(true);
    setCustomersError(null);

    return loadCustomers();
  }, [loadCustomers]);

  useEffect(() => {
    void loadCustomers();

    return () => {
      customerRequestRef.current += 1;
    };
  }, [loadCustomers]);

  // Reset period-specific fields when the supplied period changes.
  if (previousPeriod !== period) {
    setPreviousPeriod(period);
    setStartDate("");
    setEndDate("");
    setValidationError(null);
  }

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    if (loading || submittingRef.current) {
      return;
    }

    setValidationError(null);

    if (
      (startDate && !endDate) ||
      (!startDate && endDate)
    ) {
      setValidationError(
        "Select both start and end dates, or leave both empty."
      );

      return;
    }

    if (
      startDate &&
      endDate &&
      (
        startDate < firstDay ||
        endDate > lastDay ||
        startDate > endDate
      )
    ) {
      setValidationError(
        "Select a valid date range within the selected billing month."
      );

      return;
    }

    const filters: GenerationFilters = {
      ...(customerId
        ? { customerId: Number(customerId) }
        : {}),
      ...(startDate && endDate
        ? { startDate, endDate }
        : {}),
    };

    submittingRef.current = true;

    try {
      await onGenerate(filters);
    } catch (error) {
      setValidationError(
        error instanceof Error
          ? error.message
          : "Failed to generate bills."
      );
    } finally {
      submittingRef.current = false;
    }
  }

  return (
    <Card className="interactive-surface p-6">
      <form
        onSubmit={handleSubmit}
        className="space-y-5"
      >
        <div>
          <h2 className="text-lg font-semibold">
            Generate Bills
          </h2>

          <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
            Generate bills from unbilled meals.
            Previously billed meals are excluded.
          </p>
        </div>

        <fieldset
          disabled={loading}
          className="space-y-5"
        >
          <legend className="sr-only">
            Bill generation options
          </legend>

          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label
                htmlFor="billing-month"
                className="mb-2 block text-sm font-medium"
              >
                Billing Month
              </label>

              <Select
                id="billing-month"
                fullWidth
                value={billingMonth}
                onChange={(event) =>
                  onMonthChange(
                    Number(event.target.value)
                  )
                }
              >
                {MONTH_OPTIONS.map((month, index) => (
                  <option
                    key={month}
                    value={index + 1}
                  >
                    {month}
                  </option>
                ))}
              </Select>
            </div>

            <div>
              <label
                htmlFor="billing-year"
                className="mb-2 block text-sm font-medium"
              >
                Billing Year
              </label>

              <Select
                id="billing-year"
                fullWidth
                value={billingYear}
                onChange={(event) =>
                  onYearChange(
                    Number(event.target.value)
                  )
                }
              >
                {YEAR_OPTIONS.map((year) => (
                  <option key={year} value={year}>
                    {year}
                  </option>
                ))}
              </Select>
            </div>
          </div>

          <div>
            <label
              htmlFor="billing-customer"
              className="mb-2 block text-sm font-medium"
            >
              Customer
            </label>

            <Select
              id="billing-customer"
              fullWidth
              value={customerId}
              disabled={
                customersLoading ||
                customersError !== null
              }
              onChange={(event) =>
                setCustomerId(event.target.value)
              }
            >
              <option value="">
                All eligible customers
              </option>

              {customers.map((customer) => (
                <option
                  key={customer.customerId}
                  value={customer.customerId}
                >
                  {customer.fullName}
                  {" · "}
                  {customer.mobileNumber}
                  {customer.status === "INACTIVE"
                    ? " · Inactive"
                    : ""}
                </option>
              ))}
            </Select>

            {customersLoading && (
              <p className="mt-2 text-xs text-[var(--color-text-secondary)]">
                Loading customers...
              </p>
            )}

            {customersError && (
              <div className="mt-2 space-y-2">
                <p
                  role="alert"
                  className="text-sm text-red-500"
                >
                  {customersError}
                </p>

                <Button
                  type="button"
                  size="sm"
                  variant="secondary"
                  onClick={() => void fetchCustomers()}
                >
                  Retry Customers
                </Button>

                <p className="text-xs text-[var(--color-text-secondary)]">
                  You can still generate bills for all
                  customers when none is selected.
                </p>
              </div>
            )}
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label
                htmlFor="billing-start-date"
                className="mb-2 block text-sm font-medium"
              >
                Start Date — optional
              </label>

              <Input
                id="billing-start-date"
                fullWidth
                type="date"
                min={firstDay}
                max={lastDay}
                value={startDate}
                onChange={(event) => {
                  setStartDate(event.target.value);
                  setValidationError(null);
                }}
              />
            </div>

            <div>
              <label
                htmlFor="billing-end-date"
                className="mb-2 block text-sm font-medium"
              >
                End Date — optional
              </label>

              <Input
                id="billing-end-date"
                fullWidth
                type="date"
                min={startDate || firstDay}
                max={lastDay}
                value={endDate}
                onChange={(event) => {
                  setEndDate(event.target.value);
                  setValidationError(null);
                }}
              />
            </div>
          </div>

          <p className="text-xs text-[var(--color-text-secondary)]">
            Leave both dates empty to include the
            entire selected month. Only recorded,
            unbilled meals are included.
          </p>
        </fieldset>

        {validationError && (
          <p
            role="alert"
            className="text-sm text-red-500"
          >
            {validationError}
          </p>
        )}

        <div className="flex justify-end">
          <Button
            type="submit"
            disabled={loading}
          >
            {loading
              ? "Generating..."
              : customerId
                ? "Generate Customer Bill"
                : "Generate Bills"}
          </Button>
        </div>
      </form>
    </Card>
  );

}
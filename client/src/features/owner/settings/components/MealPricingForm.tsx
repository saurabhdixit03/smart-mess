import {
  useRef,
  useState,
  type FormEvent,
} from "react";

import Button from "@/components/common/ui/Button/Button";
import Input from "@/components/common/ui/Input/Input";

import type {
  MealPricingResponse,
  UpdateMealPricingRequest,
} from "../types";

type MealPricingFormProps = {
  pricing: MealPricingResponse | null;
  saving: boolean;
  mode?: "IMMEDIATE" | "SCHEDULED" | "EDIT_SCHEDULED";

  onUpdate: (
    payload: UpdateMealPricingRequest
  ) => Promise<boolean>;

  onSuccess?: () => void;
};

const PRICE_FIELDS = [
  { key: "fullMealPrice", label: "Full Meal" },
  { key: "halfMealPrice", label: "Half Meal" },
  { key: "extraRotiPrice", label: "Extra Roti" },
] as const;

type PriceField = (typeof PRICE_FIELDS)[number]["key"];
type PriceValues = Record<PriceField, string>;

function getTomorrow(): string {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Kolkata",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(new Date());

  const value = (type: string) =>
    parts.find((part) => part.type === type)!.value;

  const date = new Date(
    `${value("year")}-${value("month")}-${value("day")}T00:00:00Z`
  );

  date.setUTCDate(date.getUTCDate() + 1);

  return date.toISOString().slice(0, 10);
}

function isValidDate(value: string): boolean {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return false;
  }

  const date = new Date(`${value}T00:00:00Z`);

  return (
    !Number.isNaN(date.getTime()) &&
    date.toISOString().slice(0, 10) === value
  );
}

export default function MealPricingForm(
  props: MealPricingFormProps
) {
  const formKey = JSON.stringify([
    props.mode,
    props.pricing,
  ]);

  return (
    <MealPricingFormContent
      key={formKey}
      {...props}
    />
  );
}

function MealPricingFormContent({
  pricing,
  saving,
  mode = "SCHEDULED",
  onUpdate,
  onSuccess,
}: MealPricingFormProps) {
  const immediate = mode === "IMMEDIATE";
  const editing = mode === "EDIT_SCHEDULED";

  const [prices, setPrices] = useState<PriceValues>(() => ({
    fullMealPrice: pricing
      ? String(pricing.fullMealPrice)
      : "",
    halfMealPrice: pricing
      ? String(pricing.halfMealPrice)
      : "",
    extraRotiPrice: pricing
      ? String(pricing.extraRotiPrice)
      : "",
  }));

  const [effectiveDate, setEffectiveDate] = useState(
    () =>
      editing
        ? pricing?.effectiveFrom.slice(0, 10) ?? ""
        : getTomorrow()
  );

  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const submittingRef = useRef(false);
  const busy = saving || submitting;

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    if (saving || submittingRef.current) {
      return;
    }

    setError(null);

    for (const field of PRICE_FIELDS) {
      const value = prices[field.key].trim();
      const amount = Number(value);

      if (
        !/^\d+(?:\.\d{1,2})?$/.test(value) ||
        !Number.isFinite(amount) ||
        amount <= 0 ||
        amount > 99_999_999.99
      ) {
        setError(
          `${field.label} must be between ₹0.01 and ₹99,999,999.99, with at most two decimal places.`
        );
        return;
      }
    }

    if (
      !immediate &&
      (
        !isValidDate(effectiveDate) ||
        effectiveDate < getTomorrow()
      )
    ) {
      setError(
        editing
          ? "This change may already have taken effect. Close the form and refresh pricing."
          : "Select a future effective date."
      );
      return;
    }

    const payload: UpdateMealPricingRequest = {
      fullMealPrice: Number(prices.fullMealPrice),
      halfMealPrice: Number(prices.halfMealPrice),
      extraRotiPrice: Number(prices.extraRotiPrice),
      ...(!immediate ? { effectiveDate } : {}),
    };

    submittingRef.current = true;
    setSubmitting(true);

    let success = false;

    try {
      success = await onUpdate(payload);
    } catch (err: unknown) {
      setError(
        err instanceof Error
          ? err.message
          : "Unable to save meal prices."
      );
    } finally {
      submittingRef.current = false;
      setSubmitting(false);
    }

    if (success) {
      onSuccess?.();
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-4"
    >
      <fieldset
        disabled={busy}
        className="space-y-4"
      >
        <legend className="sr-only">
          {immediate ? "Initial meal prices" : "Upcoming meal prices"}
        </legend>

        <div className="grid gap-3 sm:grid-cols-3">
          {PRICE_FIELDS.map((field) => (
            <div key={field.key}>
              <label
                htmlFor={`pricing-${field.key}`}
                className="mb-1.5 block text-sm font-medium"
              >
                {field.label} (₹)
              </label>

              <Input
                id={`pricing-${field.key}`}
                fullWidth
                required
                type="number"
                min="0.01"
                max="99999999.99"
                step="0.01"
                value={prices[field.key]}
                onChange={(event) => {
                  const value = event.target.value;

                  setPrices((previous) => ({
                    ...previous,
                    [field.key]: value,
                  }));
                  setError(null);
                }}
              />
            </div>
          ))}
        </div>

        {immediate ? (
          <p className="text-xs leading-5 text-[var(--color-text-secondary)]">
            These prices apply immediately to newly recorded meals.
            You can plan future price changes after setup.
          </p>
        ) : (
          <div>
            <label
              htmlFor="pricing-effective-date"
              className="mb-1.5 block text-sm font-medium"
            >
              Effective Date
            </label>

            <Input
              id="pricing-effective-date"
              fullWidth
              required
              type="date"
              min={getTomorrow()}
              readOnly={editing}
              value={effectiveDate}
              onChange={(event) => {
                setEffectiveDate(event.target.value);
                setError(null);
              }}
            />

            <p className="mt-2 text-xs leading-5 text-[var(--color-text-secondary)]">
              {editing
                ? "To change the date, delete this change and plan a new one."
                : "Prices apply from midnight on this date in India time."}
            </p>
          </div>
        )}
      </fieldset>

      {error && (
        <p role="alert" className="text-sm text-red-500">
          {error}
        </p>
      )}

      <div className="flex justify-end border-t border-[var(--color-border)] pt-4">
        <Button type="submit" disabled={busy}>
          {busy
            ? "Saving..."
            : immediate
              ? "Set Meal Prices"
              : "Save"}
        </Button>
      </div>
    </form>
  );
}
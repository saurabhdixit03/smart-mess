
import { useEffect, useState } from "react";

import Button from "@/components/common/ui/Button/Button";
import Input from "@/components/common/ui/Input/Input";

import { useMealPricing } from "../hooks";

import type { UpdateMealPricingRequest } from "../types";

type PricingFormState = {
  halfMealPrice: string;
  fullMealPrice: string;
  extraRotiPrice: string;
};

export default function MealPricingForm() {
  const {
    pricing,
    loading,
    saving,
    updatePricing,
  } = useMealPricing();

  const [form, setForm] =
    useState<PricingFormState>({
      halfMealPrice: "",
      fullMealPrice: "",
      extraRotiPrice: "",
    });

  useEffect(() => {
    if (!pricing) {
      return;
    }

    setForm({
      halfMealPrice: String(pricing.halfMealPrice),
      fullMealPrice: String(pricing.fullMealPrice),
      extraRotiPrice: String(pricing.extraRotiPrice),
    });
  }, [pricing]);

  function handleChange(
    field: keyof PricingFormState,
    value: string
  ) {
    setForm((previous) => ({
      ...previous,
      [field]: value,
    }));
  }

  async function handleSubmit(
    e: React.FormEvent
  ) {
    e.preventDefault();

    const halfMealPrice = Number(form.halfMealPrice);
    const fullMealPrice = Number(form.fullMealPrice);
    const extraRotiPrice = Number(form.extraRotiPrice);

    if (
      halfMealPrice <= 0 ||
      fullMealPrice <= 0 ||
      extraRotiPrice <= 0
    ) {
      return;
    }

    const payload: UpdateMealPricingRequest = {
      halfMealPrice,
      fullMealPrice,
      extraRotiPrice,
    };

    await updatePricing(payload);
  }

  if (loading) {
    return (
      <p className="text-sm text-[var(--color-text-secondary)]">
        Loading meal pricing...
      </p>
    );
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-6"
    >
      <div
        className="
          grid
          gap-5
          md:grid-cols-3
        "
      >
        <div>
          <label className="mb-2 block text-sm font-medium">
            Half Meal
          </label>

          <Input
            fullWidth
            type="number"
            min="1"
            value={form.halfMealPrice}
            onChange={(e) =>
              handleChange(
                "halfMealPrice",
                e.target.value
              )
            }
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium">
            Full Meal
          </label>

          <Input
            fullWidth
            type="number"
            min="1"
            value={form.fullMealPrice}
            onChange={(e) =>
              handleChange(
                "fullMealPrice",
                e.target.value
              )
            }
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium">
            Extra Roti
          </label>

          <Input
            fullWidth
            type="number"
            min="1"
            value={form.extraRotiPrice}
            onChange={(e) =>
              handleChange(
                "extraRotiPrice",
                e.target.value
              )
            }
          />
        </div>
      </div>

      {pricing && (
        <p className="text-sm text-[var(--color-text-secondary)]">
          Last updated:{" "}
          {new Date(
            pricing.updatedAt
          ).toLocaleString()}
        </p>
      )}

      <div className="flex justify-end">
        <Button
          type="submit"
          disabled={saving}
        >
          {saving
            ? "Updating..."
            : "Update Pricing"}
        </Button>
      </div>
    </form>
  );
}

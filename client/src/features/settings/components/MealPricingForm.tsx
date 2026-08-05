import { useEffect, useState } from "react";

import Button from "@/components/common/ui/Button/Button";
import Input from "@/components/common/ui/Input/Input";

import { useMealPricing } from "../hooks";

import type { UpdateMealPricingRequest } from "../types";

export default function MealPricingForm() {
  const {
    pricing,
    loading,
    saving,
    updatePricing,
  } = useMealPricing();

  const [form, setForm] =
    useState<UpdateMealPricingRequest>({
      halfMealPrice: 0,
      fullMealPrice: 0,
      extraRotiPrice: 0,
    });

  useEffect(() => {
    if (!pricing) {
      return;
    }

    setForm({
      halfMealPrice: pricing.halfMealPrice,
      fullMealPrice: pricing.fullMealPrice,
      extraRotiPrice: pricing.extraRotiPrice,
    });
  }, [pricing]);

  function handleChange(
    field: keyof UpdateMealPricingRequest,
    value: string
  ) {
    setForm((previous) => ({
      ...previous,
      [field]: Number(value),
    }));
  }

  async function handleSubmit(
    e: React.FormEvent
  ) {
    e.preventDefault();

    if (
      form.halfMealPrice <= 0 ||
      form.fullMealPrice <= 0 ||
      form.extraRotiPrice <= 0
    ) {
      return;
    }

    await updatePricing(form);
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
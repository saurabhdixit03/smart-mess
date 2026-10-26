import { useState } from "react";

import {
  Label,
  Input,
  Button,
} from "@/components/common/ui";

import Modal from "@/components/common/ui/Modal";

import { usePublishMenuForm } from "../../hooks/usePublishMenuForm";

import type { DishCategory } from "../../types/dish.types";

type DishFieldName =
  | "sabjiOne"
  | "sabjiTwo"
  | "dal"
  | "rice"
  | "sweet";

type PublishMenuDialogProps = {
  open: boolean;
  title: string;
  mealSession: "LUNCH" | "DINNER";
  onSuccess: () => Promise<void>;
  onClose: () => void;
};

export default function PublishMenuDialog({
  open,
  title,
  mealSession,
  onSuccess,
  onClose,
}: PublishMenuDialogProps) {
  const [activeField, setActiveField] =
    useState<DishFieldName | null>(null);

  const [highlightedIndex, setHighlightedIndex] =
    useState(0);

  const {
    form: {
      register,
      handleSubmit,
      setValue,
      formState: { errors },
    },
    onSubmit,
    isSubmitting,
    dishSuggestions,
    isSearchingDishes,
    searchDishSuggestions,
    clearDishSuggestions,
  } = usePublishMenuForm({
    mealSession,
    onSuccess,
    onCancel: onClose,
  });

  const selectDish = (
    fieldName: DishFieldName,
    category: DishCategory,
    dishName: string
  ) => {
    setValue(fieldName, dishName, {
      shouldValidate: true,
      shouldDirty: true,
    });

    clearDishSuggestions(category);
    setActiveField(null);
    setHighlightedIndex(0);
  };

  const renderDishField = (
    fieldName: DishFieldName,
    label: string,
    category: DishCategory,
    placeholder: string,
    required = false
  ) => {
    const suggestions = dishSuggestions[category];
    const fieldError = errors[fieldName];

    const showSuggestions =
      activeField === fieldName &&
      suggestions.length > 0;

    const showSearching =
      activeField === fieldName &&
      isSearchingDishes &&
      suggestions.length === 0;

    const registeredField = register(fieldName);

    return (
      <div className="space-y-2">
        <Label htmlFor={fieldName}>
          {label}
          {required ? " *" : ""}
        </Label>

        <div className="relative">
          <Input
            id={fieldName}
            fullWidth
            placeholder={placeholder}
            error={!!fieldError}
            autoComplete="off"
            {...registeredField}
            onFocus={() => {
              setActiveField(fieldName);
              setHighlightedIndex(0);
            }}
            onChange={(event) => {
              registeredField.onChange(event);

              setActiveField(fieldName);
              setHighlightedIndex(0);

              searchDishSuggestions(
                category,
                event.target.value
              );
            }}
            onKeyDown={(event) => {
              if (!showSuggestions) {
                if (
                  event.key === "ArrowDown" &&
                  suggestions.length > 0
                ) {
                  event.preventDefault();

                  setActiveField(fieldName);
                  setHighlightedIndex(0);
                }

                return;
              }

              if (event.key === "ArrowDown") {
                event.preventDefault();

                setHighlightedIndex(
                  (current) =>
                    (current + 1) %
                    suggestions.length
                );

                return;
              }

              if (event.key === "ArrowUp") {
                event.preventDefault();

                setHighlightedIndex(
                  (current) =>
                    current === 0
                      ? suggestions.length - 1
                      : current - 1
                );

                return;
              }

              if (event.key === "Enter") {
                event.preventDefault();

                const selectedDish =
                  suggestions[highlightedIndex];

                if (selectedDish) {
                  selectDish(
                    fieldName,
                    category,
                    selectedDish.name
                  );
                }

                return;
              }

              if (event.key === "Escape") {
                event.preventDefault();

                clearDishSuggestions(category);
                setActiveField(null);
                setHighlightedIndex(0);
              }
            }}
            onBlur={() => {
              setTimeout(() => {
                if (activeField === fieldName) {
                  clearDishSuggestions(category);
                  setActiveField(null);
                  setHighlightedIndex(0);
                }
              }, 150);
            }}
          />

          {showSuggestions && (
            <div className="absolute z-50 mt-1 w-full overflow-hidden rounded-md border border-gray-200 bg-white shadow-lg">
              {suggestions.map((dish, index) => (
                <button
                  key={dish.dishId}
                  type="button"
                  className={`block w-full px-3 py-2 text-left text-sm ${
                    index === highlightedIndex
                      ? "bg-gray-100"
                      : "hover:bg-gray-100"
                  }`}
                  onMouseDown={(event) => {
                    event.preventDefault();

                    selectDish(
                      fieldName,
                      category,
                      dish.name
                    );
                  }}
                  onMouseEnter={() => {
                    setHighlightedIndex(index);
                  }}
                >
                  {dish.name}
                </button>
              ))}
            </div>
          )}

          {showSearching && (
            <div className="absolute z-50 mt-1 w-full rounded-md border border-gray-200 bg-white px-3 py-2 text-sm text-gray-500 shadow-lg">
              Searching...
            </div>
          )}
        </div>

        {fieldError && (
          <p className="text-sm text-red-500">
            {fieldError.message}
          </p>
        )}
      </div>
    );
  };

  return (
    <Modal
      open={open}
      title={title}
      onClose={onClose}
      footer={
        <>
          <Button
            variant="secondary"
            onClick={onClose}
            disabled={isSubmitting}
          >
            Cancel
          </Button>

          <Button
            type="submit"
            form="publish-menu-form"
            disabled={isSubmitting}
          >
            {isSubmitting
              ? "Publishing..."
              : "Publish Menu"}
          </Button>
        </>
      }
    >
      <form
        id="publish-menu-form"
        onSubmit={handleSubmit(onSubmit)}
        className="space-y-4"
      >
        {renderDishField(
          "sabjiOne",
          "Sabji 1",
          "SABJI",
          "Enter first sabji",
          true
        )}

        {renderDishField(
          "sabjiTwo",
          "Sabji 2",
          "SABJI",
          "Enter second sabji"
        )}

        {renderDishField(
          "dal",
          "Dal",
          "DAL",
          "Enter dal"
        )}

        {renderDishField(
          "rice",
          "Rice",
          "RICE",
          "Enter rice"
        )}

        {renderDishField(
          "sweet",
          "Sweet",
          "SWEET",
          "Enter sweet"
        )}

          <p className="text-sm text-gray-500">
          <span className="font-medium">Tip:</span>{" "}
          Use proper spelling for new dishes. Previously used dishes will be suggested automatically for consistent menu insights.
        </p>

      </form>
    </Modal>
  );
}

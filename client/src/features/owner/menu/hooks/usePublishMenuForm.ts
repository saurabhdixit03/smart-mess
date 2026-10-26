import { useState } from "react";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { toast } from "sonner";

import { dishApi } from "../api/dish.api";
import { menuApi } from "../api/menu.api";

import {
  MENU_FORM,
  MENU_MESSAGES,
} from "../constants/menu.constants";

import {
  menuSchema,
  type PublishMenuFormData,
} from "../schemas/menu.schema";

import type { CreateMenuRequest } from "../types/menu.types";
import type {
  DishCategory,
  DishResponse,
} from "../types/dish.types";

type UsePublishMenuFormProps = {
  mealSession: "LUNCH" | "DINNER";
  onSuccess: () => Promise<void>;
  onCancel: () => void;
};

export function usePublishMenuForm({
  mealSession,
  onSuccess,
  onCancel,
}: UsePublishMenuFormProps) {
  const [isSubmitting, setIsSubmitting] =
    useState(false);

  const [dishSuggestions, setDishSuggestions] =
    useState<Record<DishCategory, DishResponse[]>>({
      SABJI: [],
      DAL: [],
      RICE: [],
      SWEET: [],
    });

  const [isSearchingDishes, setIsSearchingDishes] =
    useState(false);

  const form = useForm<PublishMenuFormData>({
    resolver: zodResolver(menuSchema),
    defaultValues: MENU_FORM.DEFAULT_VALUES,
    mode: "onBlur",
  });

  const searchDishSuggestions = async (
    category: DishCategory,
    query: string
  ) => {
    if (!query.trim()) {
      setDishSuggestions((current) => ({
        ...current,
        [category]: [],
      }));

      return;
    }

    try {
      setIsSearchingDishes(true);

      const response =
        await dishApi.searchDishes(
          category,
          query
        );

      setDishSuggestions((current) => ({
        ...current,
        [category]: response.data,
      }));
    } catch (error) {
      console.error(error);

      setDishSuggestions((current) => ({
        ...current,
        [category]: [],
      }));
    } finally {
      setIsSearchingDishes(false);
    }
  };

  const clearDishSuggestions = (
    category: DishCategory
  ) => {
    setDishSuggestions((current) => ({
      ...current,
      [category]: [],
    }));
  };

  const onSubmit = async (
    data: PublishMenuFormData
  ) => {
    try {
      setIsSubmitting(true);

      const today = new Date();

      const menuDate = [
        today.getFullYear(),
        String(today.getMonth() + 1).padStart(2, "0"),
        String(today.getDate()).padStart(2, "0"),
      ].join("-");

      const payload: CreateMenuRequest = {
        ...data,
        mealSession,
        menuDate,
      };

      await menuApi.publishMenu(payload);

      toast.success(
        MENU_MESSAGES.PUBLISH_SUCCESS
      );

      form.reset(MENU_FORM.DEFAULT_VALUES);

      setDishSuggestions({
        SABJI: [],
        DAL: [],
        RICE: [],
        SWEET: [],
      });

      await onSuccess();

      onCancel();

    } catch (error) {

      console.error(error);

      toast.error(
        MENU_MESSAGES.PUBLISH_FAILED
      );

    } finally {

      setIsSubmitting(false);

    }
  };

  return {
    form,
    onSubmit,
    isSubmitting,
    dishSuggestions,
    isSearchingDishes,
    searchDishSuggestions,
    clearDishSuggestions,
  };
}

import { useState } from "react";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { toast } from "sonner";

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
  const [isSubmitting, setIsSubmitting] = useState(false);

  const form = useForm<PublishMenuFormData>({
    resolver: zodResolver(menuSchema),
    defaultValues: MENU_FORM.DEFAULT_VALUES,
    mode: "onBlur",
  });

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
  };
}
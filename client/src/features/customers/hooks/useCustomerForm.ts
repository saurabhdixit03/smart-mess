
import { useEffect, useState } from "react";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { toast } from "sonner";

import { customerSchema } from "../schemas/customer.schema";

import type { CustomerFormData } from "../schemas/customer.schema";
import type { CustomerResponse } from "../types/customer.types";

import { CUSTOMER_FORM } from "../constants/customer.constants";
import { customerApi } from "../api/customer.api";

export const useCustomerForm = (
  selectedCustomer: CustomerResponse | null,
  onSuccess: () => Promise<void>,
  onCancel: () => void
) => {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const form = useForm<CustomerFormData>({
    resolver: zodResolver(customerSchema),

    defaultValues: CUSTOMER_FORM.DEFAULT_VALUES,

    mode: "onBlur",
  });

  useEffect(() => {
    if (selectedCustomer) {
      form.reset({
        remarks: selectedCustomer.remarks ?? "",
      });
    } else {
      form.reset(
        CUSTOMER_FORM.DEFAULT_VALUES
      );
    }
  }, [selectedCustomer, form]);

  const onSubmit = async (data: CustomerFormData) => {
    if (!selectedCustomer) {
      return;
    }

    try {
      setIsSubmitting(true);

      await customerApi.updateCustomer(
        selectedCustomer.customerId,
        data
      );

      toast.success(
        "Customer remarks updated successfully."
      );

      form.reset(
        CUSTOMER_FORM.DEFAULT_VALUES
      );

      await onSuccess();

      onCancel();

    } catch (error) {
      console.error(error);

      toast.error(
        "Failed to update customer remarks."
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
};

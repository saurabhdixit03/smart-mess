import {
  Label,
  Input,
  Textarea,
  Button,
} from "@/components/common/ui";

import Modal from "@/components/common/ui/Modal/Modal";

import { useCustomerForm } from "../hooks/useCustomerForm";

import type { CustomerResponse } from "../types/customer.types";

type CustomerFormProps = {
    open: boolean
    selectedCustomer: CustomerResponse | null
  onSuccess: () => Promise<void>;
  onCancel: () => void;
};

const CustomerForm = ({
  open,
  selectedCustomer,
  onSuccess,
  onCancel,
}: CustomerFormProps) => {

  const isEditMode = selectedCustomer !== null;

  const {
    form: {
      register,
      handleSubmit,
      formState: { errors },
    },
    onSubmit,
    isSubmitting,
  } = useCustomerForm(
    selectedCustomer,
    onSuccess,
    onCancel
  );

  return (
  <Modal
    open={open}
    onClose={onCancel}
    title={
        isEditMode
          ? "Edit Customer"
          : "Add Customer"
      }
      footer={
        <>
          <Button
            variant="secondary"
            onClick={onCancel}
            disabled={isSubmitting}
          >
            Cancel
          </Button>

          <Button
            type="submit"
            form="customer-form"
            disabled={isSubmitting}
          >
            {isSubmitting
              ? isEditMode
                ? "Updating..."
                : "Saving..."
              : isEditMode
                ? "Update Customer"
                : "Save Customer"}
          </Button>
        </>
      }
    >
      <form
        id="customer-form"
        onSubmit={handleSubmit(onSubmit)}
        className="space-y-5"
      >

        {/* Full Name */}

        <div className="space-y-2">

          <Label htmlFor="fullName">
            Full Name *
          </Label>

          <Input
            id="fullName"
            fullWidth
            placeholder="Enter full name"
            error={!!errors.fullName}
            {...register("fullName")}
          />

          {errors.fullName && (

            <p className="text-sm text-red-500">
              {errors.fullName.message}
            </p>

          )}

        </div>

        {/* Mobile */}

        <div className="space-y-2">

          <Label htmlFor="mobileNumber">
            Mobile Number *
          </Label>

          <Input
            id="mobileNumber"
            fullWidth
            placeholder="Enter mobile number"
            error={!!errors.mobileNumber}
            {...register("mobileNumber")}
          />

          {errors.mobileNumber && (

            <p className="text-sm text-red-500">
              {errors.mobileNumber.message}
            </p>

          )}

        </div>

        {/* Email */}

        <div className="space-y-2">

          <Label htmlFor="email">
            Email
          </Label>

          <Input
            id="email"
            type="email"
            fullWidth
            placeholder="Enter email"
            error={!!errors.email}
            {...register("email")}
          />

          {errors.email && (

            <p className="text-sm text-red-500">
              {errors.email.message}
            </p>

          )}

        </div>

        {/* Remarks */}

        <div className="space-y-2">

          <Label htmlFor="remarks">
            Remarks
          </Label>

          <Textarea
            id="remarks"
            rows={4}
            fullWidth
            placeholder="Additional notes..."
            error={!!errors.remarks}
            {...register("remarks")}
          />

          {errors.remarks && (

            <p className="text-sm text-red-500">
              {errors.remarks.message}
            </p>

          )}

        </div>

      </form>

    </Modal>
  );
};

export default CustomerForm;
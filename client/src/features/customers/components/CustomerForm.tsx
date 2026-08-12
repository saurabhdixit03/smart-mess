
import {
  Label,
  Textarea,
  Button,
} from "@/components/common/ui";

import Modal from "@/components/common/ui/Modal/Modal";

import { useCustomerForm } from "../hooks/useCustomerForm";

import type { CustomerResponse } from "../types/customer.types";

type CustomerFormProps = {
  open: boolean;
  selectedCustomer: CustomerResponse | null;
  onSuccess: () => Promise<void>;
  onCancel: () => void;
};

const CustomerForm = ({
  open,
  selectedCustomer,
  onSuccess,
  onCancel,
}: CustomerFormProps) => {

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
      title="Manage Customer"
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
              ? "Updating..."
              : "Update Remarks"}
          </Button>
        </>
      }
    >
      <form
        id="customer-form"
        onSubmit={handleSubmit(onSubmit)}
        className="space-y-5"
      >

        {/* Customer Information */}

        <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-background-secondary)] p-4">

          <div className="grid grid-cols-2 gap-4">

            <div>
              <p className="text-xs text-[var(--color-text-secondary)]">
                Customer ID
              </p>

              <p className="mt-1 font-semibold">
                #{selectedCustomer?.customerId ?? "-"}
              </p>
            </div>

            <div>
              <p className="text-xs text-[var(--color-text-secondary)]">
                Status
              </p>

              <p className="mt-1 font-semibold">
                {selectedCustomer?.status ?? "-"}
              </p>
            </div>

            <div>
              <p className="text-xs text-[var(--color-text-secondary)]">
                Customer
              </p>

              <p className="mt-1 font-semibold">
                {selectedCustomer?.fullName ?? "-"}
              </p>
            </div>

            <div>
              <p className="text-xs text-[var(--color-text-secondary)]">
                Mobile
              </p>

              <p className="mt-1 font-semibold">
                {selectedCustomer?.mobileNumber ?? "-"}
              </p>
            </div>

          </div>

        </div>

        {/* Remarks */}

        <div className="space-y-2">

          <Label htmlFor="remarks">
            Remarks
          </Label>

          <Textarea
            id="remarks"
            rows={5}
            fullWidth
            placeholder="Add remarks about the customer..."
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


import {
  Label,
  Input,
  Button,
} from "@/components/common/ui";

import Modal from "@/components/common/ui/Modal";

import { usePublishMenuForm } from "../../hooks/usePublishMenuForm";

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

  const {
    form: {
      register,
      handleSubmit,
      formState: { errors },
    },
    onSubmit,
    isSubmitting,
  } = usePublishMenuForm({
    mealSession,
    onSuccess,
    onCancel: onClose,
  });

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
        {/* Sabji 1 */}

        <div className="space-y-2">
          <Label htmlFor="sabjiOne">
            Sabji 1 *
          </Label>

          <Input
            id="sabjiOne"
            autoFocus
            fullWidth
            placeholder="Enter first sabji"
            error={!!errors.sabjiOne}
            {...register("sabjiOne")}
          />

          {errors.sabjiOne && (
            <p className="text-sm text-red-500">
              {errors.sabjiOne.message}
            </p>
          )}
        </div>

        {/* Sabji 2 */}

        <div className="space-y-2">
          <Label htmlFor="sabjiTwo">
            Sabji 2
          </Label>

          <Input
            id="sabjiTwo"
            fullWidth
            placeholder="Enter second sabji"
            error={!!errors.sabjiTwo}
            {...register("sabjiTwo")}
          />

          {errors.sabjiTwo && (
            <p className="text-sm text-red-500">
              {errors.sabjiTwo.message}
            </p>
          )}
        </div>

        {/* Dal */}

        <div className="space-y-2">
          <Label htmlFor="dal">
            Dal
          </Label>

          <Input
            id="dal"
            fullWidth
            placeholder="Enter dal"
            error={!!errors.dal}
            {...register("dal")}
          />

          {errors.dal && (
            <p className="text-sm text-red-500">
              {errors.dal.message}
            </p>
          )}
        </div>

        {/* Rice */}

        <div className="space-y-2">
          <Label htmlFor="rice">
            Rice
          </Label>

          <Input
            id="rice"
            fullWidth
            placeholder="Enter rice"
            error={!!errors.rice}
            {...register("rice")}
          />

          {errors.rice && (
            <p className="text-sm text-red-500">
              {errors.rice.message}
            </p>
          )}
        </div>

        {/* Sweet */}

        <div className="space-y-2">
          <Label htmlFor="sweet">
            Sweet
          </Label>

          <Input
            id="sweet"
            fullWidth
            placeholder="Enter sweet"
            error={!!errors.sweet}
            {...register("sweet")}
          />

          {errors.sweet && (
            <p className="text-sm text-red-500">
              {errors.sweet.message}
            </p>
          )}
        </div>
      </form>
    </Modal>
  );
}
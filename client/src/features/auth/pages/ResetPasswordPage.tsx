import { useState } from "react";
import type { FormEvent } from "react";
import {
  useNavigate,
  useSearchParams,
} from "react-router-dom";

import { useResetPassword } from "../hooks/useResetPassword";

import {
  Button,
  Card,
  Input,
  Label,
} from "@/components/common/ui";

export default function ResetPasswordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const token = searchParams.get("token");

  const roleParam = searchParams.get("role");

  const loginPath =
  roleParam === "CUSTOMER"
    ? "/customer/login"
    : "/owner/login";

  const {
    resetPassword,
    loading,
    error,
    success,
  } = useResetPassword();

  const [newPassword, setNewPassword] =
    useState("");

  const [
    confirmPassword,
    setConfirmPassword,
  ] = useState("");

  const passwordsDoNotMatch =
    newPassword.length > 0 &&
    confirmPassword.length > 0 &&
    newPassword !== confirmPassword;

  const handleSubmit = async (
    event: FormEvent
  ) => {
    event.preventDefault();

    if (!token) {
      return;
    }

    if (newPassword !== confirmPassword) {
      return;
    }

    try {
      await resetPassword({
        token,
        newPassword,
        confirmPassword,
      });
    } catch {
      // Error is already handled by the hook.
    }
  };

  if (!token) {
    return (
      <div className="flex min-h-full items-center justify-center px-4 py-8">
        <Card className="w-full max-w-md">

          <Card.Body className="py-10 text-center">

            <h1 className="text-lg font-semibold text-[var(--color-text)]">
              Invalid reset link
            </h1>

            <p className="mt-2 text-sm text-[var(--color-text-secondary)]">
              This password reset link is missing or invalid.
              Request a new reset link and try again.
            </p>

            <Button
              type="button"
              className="mt-6"
              onClick={() =>
                navigate(loginPath)
              }
            >
              Back to Home
            </Button>

          </Card.Body>

        </Card>
      </div>
    );
  }

  if (success) {
    return (
      <div className="flex min-h-full items-center justify-center px-4 py-8">

        <Card className="w-full max-w-md">

          <Card.Body className="py-10 text-center">

            <h1 className="text-lg font-semibold text-[var(--color-text)]">
              Password reset successfully
            </h1>

            <p className="mt-2 text-sm text-[var(--color-text-secondary)]">
              Your password has been updated.
              You can now sign in using your new password.
            </p>

            <Button
              type="button"
              className="mt-6"
              onClick={() =>
                navigate(loginPath)
              }
            >
              Go to Login
            </Button>

          </Card.Body>

        </Card>

      </div>
    );
  }

  return (
    <div className="flex min-h-full items-center justify-center px-4 py-8">

      <Card className="w-full max-w-md">

        <Card.Header>

          <h1 className="text-xl font-semibold text-[var(--color-text)]">
            Reset Password
          </h1>

          <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
            Create a new password for your Smart Mess account.
          </p>

        </Card.Header>

        <Card.Body>

          <form
            onSubmit={handleSubmit}
            className="space-y-5"
          >

            <div>

              <Label
                htmlFor="newPassword"
                required
              >
                New Password
              </Label>

              <Input
                id="newPassword"
                type="password"
                value={newPassword}
                onChange={(event) =>
                  setNewPassword(
                    event.target.value
                  )
                }
                placeholder="Enter new password"
                autoComplete="new-password"
                fullWidth
                required
              />

            </div>

            <div>

              <Label
                htmlFor="confirmPassword"
                required
              >
                Confirm Password
              </Label>

              <Input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(event) =>
                  setConfirmPassword(
                    event.target.value
                  )
                }
                placeholder="Confirm new password"
                autoComplete="new-password"
                fullWidth
                required
              />

            </div>

            {passwordsDoNotMatch && (
              <p className="text-sm text-red-500">
                Passwords do not match.
              </p>
            )}

            {error && (
              <p className="text-sm text-red-500">
                {error}
              </p>
            )}

            <Button
              type="submit"
              size="lg"
              fullWidth
              disabled={
                loading ||
                passwordsDoNotMatch
              }
            >
              {loading
                ? "Resetting password..."
                : "Reset Password"}
            </Button>

          </form>

        </Card.Body>

      </Card>

    </div>
  );
}
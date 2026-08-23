import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import { useForgotPassword } from "../hooks/useForgotPassword";

import type { AuthRole } from "../types/auth.types";

import {
  Button,
  Card,
  Input,
  Label,
} from "@/components/common/ui";

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const roleParam =
    searchParams.get("role");

  const role: AuthRole =
    roleParam === "CUSTOMER"
      ? "CUSTOMER"
      : "OWNER";

  const {
    requestPasswordReset,
    loading,
    error,
    success,
  } = useForgotPassword(role);

  const [email, setEmail] =
  useState("");

  const loginPath =
    role === "OWNER"
      ? "/owner/login"
      : "/customer/login";

  const handleSubmit = async (
    event: FormEvent
  ) => {
    event.preventDefault();

    try {
      await requestPasswordReset({
        email,
      });
    } catch {
      // Error is already handled by the hook.
    }
  };

  return (
    <div className="flex min-h-full items-center justify-center px-4 py-8">

      <Card className="w-full max-w-md">

        <Card.Header>
          <h1 className="text-xl font-semibold text-[var(--color-text)]">
            Forgot Password
          </h1>

          <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
  Enter your registered email address and we'll send you a password reset link.
</p>
        </Card.Header>

        <Card.Body>

          {success ? (
            <div className="space-y-5">

              <div className="rounded-lg border border-[var(--color-border)] p-4">
                <p className="text-sm text-[var(--color-text)]">
                  If an account exists with this email address, a password reset link has been sent.
                </p>
              </div>

              <Button
                type="button"
                fullWidth
                onClick={() =>
                  navigate(loginPath)
                }
              >
                Back to Login
              </Button>

            </div>
          ) : (
            <form
              onSubmit={handleSubmit}
              className="space-y-5"
            >

              <div>
                <Label
                htmlFor="email"
                required
                >
                Email
                </Label>

            <Input
                id="email"
                type="email"
                value={email}
                onChange={(event) =>
                setEmail(event.target.value)
                }
                placeholder="Enter email address"
                autoComplete="email"
                fullWidth
                required
            />
            </div>

              {error && (
                <p className="text-sm text-red-500">
                  {error}
                </p>
              )}

              <Button
                type="submit"
                size="lg"
                fullWidth
                disabled={loading}
              >
                {loading
                  ? "Sending..."
                  : "Send Reset Link"}
              </Button>

            </form>
          )}

        </Card.Body>

        {!success && (
          <Card.Footer>
            <button
              type="button"
              onClick={() =>
                navigate(loginPath)
              }
              className="w-full text-center text-sm font-semibold text-[var(--color-primary)] hover:underline"
            >
              Back to Login
            </button>
          </Card.Footer>
        )}

      </Card>

    </div>
  );
}
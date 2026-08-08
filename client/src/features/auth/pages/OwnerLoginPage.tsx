import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate } from "react-router-dom";

import { useOwnerLogin } from "../hooks/useOwnerLogin";

import {
  Button,
  Card,
  Input,
  Label,
} from "@/components/common/ui";

export default function OwnerLoginPage() {
  const navigate = useNavigate();

  const {
    login,
    loading,
    error,
  } = useOwnerLogin();

  const [mobileNumber, setMobileNumber] =
    useState("");

  const [password, setPassword] =
    useState("");

  const accountNotFound =
    error?.toLowerCase().includes("owner not found") ||
    error?.toLowerCase().includes("account not found");

  const handleSubmit = async (
    event: FormEvent
  ) => {
    event.preventDefault();

    try {
      await login({
        mobileNumber,
        password,
      });

      navigate("/owner");

    } catch {
      // Error is already handled by the hook.
    }
  };

  return (
    <div className="flex min-h-full items-center justify-center px-4 py-8">

      <Card className="w-full max-w-md">

        <Card.Header>
          <h1 className="text-xl font-semibold text-[var(--color-text)]">
            Owner Login
          </h1>

          <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
            Sign in to manage your mess
          </p>
        </Card.Header>

        <Card.Body>
          <form
            onSubmit={handleSubmit}
            className="space-y-5"
          >

            <div>
              <Label
                htmlFor="mobileNumber"
                required
              >
                Mobile Number
              </Label>

              <Input
                id="mobileNumber"
                type="tel"
                value={mobileNumber}
                onChange={(event) =>
                  setMobileNumber(event.target.value)
                }
                placeholder="Enter mobile number"
                autoComplete="tel"
                fullWidth
                required
              />
            </div>

            <div>
              <Label
                htmlFor="password"
                required
              >
                Password
              </Label>

              <Input
                id="password"
                type="password"
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
                placeholder="Enter password"
                autoComplete="current-password"
                fullWidth
                required
              />
            </div>

            {error && (
              <div className="space-y-2">

                <p className="text-sm text-red-500">
                  {accountNotFound
                    ? "No owner account found with this mobile number."
                    : error}
                </p>

                {accountNotFound && (
                  <Button
                    type="button"
                    variant="outline"
                    fullWidth
                    onClick={() =>
                      navigate("/owner/register")
                    }
                  >
                    Register First
                  </Button>
                )}

              </div>
            )}

            <Button
              type="submit"
              size="lg"
              fullWidth
              disabled={loading}
            >
              {loading
                ? "Logging in..."
                : "Login"}
            </Button>

          </form>
        </Card.Body>

        <Card.Footer>
          <p className="text-center text-sm text-[var(--color-text-secondary)]">
            Don't have an account?{" "}

            <button
              type="button"
              onClick={() =>
                navigate("/owner/register")
              }
              className="font-semibold text-[var(--color-primary)] hover:underline"
            >
              Register
            </button>
          </p>
        </Card.Footer>

      </Card>

    </div>
  );
}
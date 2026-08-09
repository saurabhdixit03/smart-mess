import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate } from "react-router-dom";

import { useCustomerLogin } from "../hooks/useCustomerLogin";

import {
  Button,
  Card,
  Input,
  Label,
} from "@/components/common/ui";

export default function CustomerLoginPage() {
  const navigate = useNavigate();

  const {
    login,
    loading,
    error,
  } = useCustomerLogin();

  const [mobileNumber, setMobileNumber] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    try {
      await login({
        mobileNumber,
        password,
      });

      navigate("/customer");
    } catch {
      // Error is already handled by the hook.
    }
  };

  return (
    <div className="flex min-h-full items-center justify-center px-4 py-8">
      <Card className="w-full max-w-md">
        <Card.Header>
          <h1 className="text-xl font-semibold text-[var(--color-text)]">
            Customer Login
          </h1>

          <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
            Sign in to manage your meals and billing
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
              {loading ? "Logging in..." : "Login"}
            </Button>
          </form>
        </Card.Body>

        <Card.Footer>
          <p className="text-center text-sm text-[var(--color-text-secondary)]">
            Don't have an account?{" "}

            <button
              type="button"
              onClick={() =>
                navigate("/customer/register")
              }
              className="font-semibold text-[var(--color-primary)] hover:underline"
            >
              Create Account
            </button>
          </p>
        </Card.Footer>
      </Card>
    </div>
  );
}
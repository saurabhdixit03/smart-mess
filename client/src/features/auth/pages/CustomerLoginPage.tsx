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

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    try {
      await login({
        email,
        password,
      });

      navigate("/customer", {
        replace: true,
      });
    } catch {
      // The hook provides the error message.
    }
  };

  return (
    <div className="flex min-h-full items-center justify-center px-4 py-8">
      <Card className="w-full max-w-md">
        <Card.Header>
          <h1 className="text-xl font-semibold text-[var(--color-text)]">
            Customer Login
          </h1>

          <p className="mt-2 text-sm leading-6 text-[var(--color-text-secondary)]">
            Sign in to view your menu, respond to meals,
            and check your bills.
          </p>
        </Card.Header>

        <Card.Body>
          <form
            onSubmit={handleSubmit}
            className="space-y-5"
            aria-busy={loading}
          >
            <div>
              <Label htmlFor="email" required>
                Email
              </Label>

              <Input
                id="email"
                name="email"
                type="email"
                value={email}
                onChange={(event) =>
                  setEmail(event.target.value)
                }
                placeholder="Enter email address"
                autoComplete="username"
                fullWidth
                required
              />
            </div>

            <div>
              <Label htmlFor="password" required>
                Password
              </Label>

              <Input
                id="password"
                name="password"
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

            <div className="text-right">
              <button
                type="button"
                onClick={() =>
                  navigate(
                    "/forgot-password?role=CUSTOMER"
                  )
                }
                className="rounded-sm text-sm font-medium text-[var(--color-primary)] hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]"
              >
                Forgot Password?
              </button>
            </div>

            {error && (
              <p
                role="alert"
                className="text-sm text-red-500"
              >
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
      </Card>
    </div>
  );
}
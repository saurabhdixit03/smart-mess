import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";

import { useCustomerRegistration } from "../hooks/useCustomerRegistration";

import {
  Button,
  Card,
  Input,
  Label,
} from "@/components/common/ui";

export default function CustomerRegistrationPage() {
  const navigate = useNavigate();

  const {
    register,
    loading,
    error,
  } = useCustomerRegistration();

  const [fullName, setFullName] = useState("");
  const [mobileNumber, setMobileNumber] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    try {
      await register({
        fullName,
        mobileNumber,
        email: email || undefined,
        password,
      });

      toast.success(
        "Registration successful."
      );

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
            Customer Registration
          </h1>

          <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
            Create your Smart Mess customer account
          </p>
        </Card.Header>

        <Card.Body>
          <form
            onSubmit={handleSubmit}
            className="space-y-5"
          >
            <div>
              <Label htmlFor="fullName" required>
                Full Name
              </Label>

              <Input
                id="fullName"
                type="text"
                value={fullName}
                onChange={(event) =>
                  setFullName(event.target.value)
                }
                placeholder="Enter your full name"
                autoComplete="name"
                fullWidth
                required
              />
            </div>

            <div>
              <Label htmlFor="mobileNumber" required>
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
              <Label htmlFor="email">
                Email
              </Label>

              <Input
                id="email"
                type="email"
                value={email}
                onChange={(event) =>
                  setEmail(event.target.value)
                }
                placeholder="Enter email address (optional)"
                autoComplete="email"
                fullWidth
              />
            </div>

            <div>
              <Label htmlFor="password" required>
                Password
              </Label>

              <Input
                id="password"
                type="password"
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
                placeholder="Create a password"
                autoComplete="new-password"
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
                ? "Creating account..."
                : "Create Account"}
            </Button>
          </form>
        </Card.Body>

        <Card.Footer>
          <p className="text-center text-sm text-[var(--color-text-secondary)]">
            Already have an account?{" "}

            <button
              type="button"
              onClick={() => navigate("/customer/login")}
              className="font-semibold text-[var(--color-primary)] hover:underline"
            >
              Login
            </button>
          </p>
        </Card.Footer>
      </Card>
    </div>
  );
}
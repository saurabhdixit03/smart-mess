import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";

import { useOwnerRegistration } from "../hooks/useOwnerRegistration";

import {
  Button,
  Card,
  Input,
  Label,
} from "@/components/common/ui";

export default function OwnerRegistrationPage() {
  const navigate = useNavigate();

  const {
    register,
    loading,
    error,
  } = useOwnerRegistration();

  const [fullName, setFullName] = useState("");
  const [messName, setMessName] = useState("");
  const [mobileNumber, setMobileNumber] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    try {
      await register({
        fullName,
        messName,
        mobileNumber,
        email,
        password,
      });

      toast.success(
        "Registration successful. Please log in to continue."
      );

      navigate("/owner/login");

    } catch {
      // Error is already handled by the hook.
    }
  };

  return (
    <div className="flex min-h-full items-center justify-center px-4 py-8">

      <Card className="w-full max-w-lg">

        <Card.Header>
          <h1 className="text-xl font-semibold text-[var(--color-text)]">
            Owner Registration
          </h1>

          <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
            Create your Smart Mess owner account
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
              <Label htmlFor="messName" required>
                Mess Name
              </Label>

              <Input
                id="messName"
                type="text"
                value={messName}
                onChange={(event) =>
                  setMessName(event.target.value)
                }
                placeholder="Enter your mess name"
                autoComplete="organization"
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
              <Label htmlFor="email" required>
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
                placeholder="Enter password"
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
              onClick={() =>
                navigate("/owner/login")
              }
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
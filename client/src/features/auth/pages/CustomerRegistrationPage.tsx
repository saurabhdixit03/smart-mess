import {
  useEffect,
  useState,
} from "react";

import type { FormEvent } from "react";

import {
  useNavigate,
  useSearchParams,
} from "react-router-dom";

import { toast } from "sonner";

import {
  getMessRegistrationInfo,
} from "../api/messRegistration.api";

import {
  useCustomerRegistration,
} from "../hooks/useCustomerRegistration";

import type {
  CustomerRegistrationResponse,
} from "../types/auth.types";

import {
  Button,
  Card,
  Input,
  Label,
} from "@/components/common/ui";

interface RegistrationLinkState {
  code: string;
  messName: string | null;
  loading: boolean;
  error: string | null;
}

const REGISTRATION_CODE_PATTERN =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export default function CustomerRegistrationPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const registrationCode =
    searchParams.get("registrationCode") ?? "";

  const {
    register,
    loading,
    error,
  } = useCustomerRegistration();

  const [fullName, setFullName] = useState("");
  const [mobileNumber, setMobileNumber] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [retryCount, setRetryCount] =
    useState(0);

  const [submittedRegistration, setSubmittedRegistration] =
    useState<CustomerRegistrationResponse | null>(
      null
    );

  const [linkState, setLinkState] =
    useState<RegistrationLinkState>({
      code: "",
      messName: null,
      loading: false,
      error: null,
    });

  const hasValidCodeFormat =
    REGISTRATION_CODE_PATTERN.test(
      registrationCode
    );

  /*
   * Verify the mess associated with the URL.
   *
   * Cancel the previous lookup if the registration
   * link changes or the page unmounts.
   */
  useEffect(() => {
    if (!hasValidCodeFormat) {
      return;
    }

    const controller =
      new AbortController();

    let disposed = false;

    setLinkState({
      code: registrationCode,
      messName: null,
      loading: true,
      error: null,
    });

    const loadMess = async () => {
      try {
        const info =
          await getMessRegistrationInfo(
            registrationCode,
            controller.signal
          );

        if (disposed) {
          return;
        }

        setLinkState({
          code: registrationCode,
          messName: info.messName,
          loading: false,
          error: null,
        });
      } catch (lookupError) {
        if (disposed) {
          return;
        }

        setLinkState({
          code: registrationCode,
          messName: null,
          loading: false,
          error:
            lookupError instanceof Error
              ? lookupError.message
              : "Unable to verify this registration link. Please try again.",
        });
      }
    };

    void loadMess();

    return () => {
      disposed = true;
      controller.abort();
    };
  }, [
    registrationCode,
    hasValidCodeFormat,
    retryCount,
  ]);

  /*
   * Require a successful lookup for the current code.
   * A previous link's result must not enable this form.
   */
  const currentLinkVerified =
    hasValidCodeFormat &&
    linkState.code === registrationCode &&
    !linkState.loading &&
    !linkState.error &&
    linkState.messName !== null;

  const verifyingLink =
    hasValidCodeFormat &&
    (
      linkState.code !== registrationCode ||
      linkState.loading
    );

  const linkError =
    !registrationCode
      ? "Please use the registration link or QR code provided by your mess owner."
      : !hasValidCodeFormat
        ? "This registration link is invalid. Please ask your mess owner for the correct link."
        : linkState.code === registrationCode
          ? linkState.error
          : null;

  const loginPath = registrationCode
    ? "/customer/login?registrationCode=" +
      encodeURIComponent(registrationCode)
    : "/customer/login";

  const handleSubmit = async (
    event: FormEvent
  ) => {
    event.preventDefault();

    if (
      !currentLinkVerified ||
      loading ||
      submittedRegistration
    ) {
      return;
    }

    try {
      const response = await register({
        fullName,
        mobileNumber,
        email,
        password,
        registrationCode,
      });

      setSubmittedRegistration(response);
      setPassword("");

      toast.success(
        "Registration submitted. Please wait for owner approval."
      );
    } catch {
      // Error is already handled by the hook.
    }
  };

  /*
   * A pending customer remains outside the portal.
   * They sign in normally after the owner approves them.
   */
  if (submittedRegistration) {
    return (
      <div className="flex min-h-full items-center justify-center px-4 py-8">
        <Card className="w-full max-w-md">
          <Card.Header>
            <h1 className="text-xl font-semibold text-[var(--color-text)]">
              Registration Submitted
            </h1>
          </Card.Header>

          <Card.Body>
            <div className="space-y-5">
              <p className="text-sm text-[var(--color-text-secondary)]">
                Thank you,{" "}
                <strong className="text-[var(--color-text)]">
                  {submittedRegistration.fullName}
                </strong>
              .
              </p>

            <div className="rounded-xl border border-[var(--color-border)] p-4">
              <p className="font-semibold text-[var(--color-text)]">
                  Awaiting Owner Approval
              </p>

              <p className="mt-2 text-sm text-[var(--color-text-secondary)]">
                  Your registration is awaiting approval.
                  Once approved, you’ll receive an email
                  with a link to sign in.
              </p>
            </div>

          </div>
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
            Customer Registration
          </h1>

          <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
            Create your Smart Mess customer account
          </p>

          {currentLinkVerified && (
            <div className="mt-4 rounded-lg border border-[var(--color-primary)] p-3">
              <p className="text-sm text-[var(--color-text-secondary)]">
                You are joining
              </p>

              <p className="mt-1 font-semibold text-[var(--color-text)]">
                {linkState.messName}
              </p>

              <p className="mt-2 text-sm text-[var(--color-text-secondary)]">
                Owner approval is required before
                you can sign in.
              </p>
            </div>
          )}
        </Card.Header>

        <Card.Body>
          {verifyingLink && (
            <p
              role="status"
              className="text-sm text-[var(--color-text-secondary)]"
            >
              Checking your mess registration link...
            </p>
          )}

          {linkError && (
            <div className="space-y-4">
              <p
                role="alert"
                className="text-sm text-red-500"
              >
                {linkError}
              </p>

              {hasValidCodeFormat && (
                <Button
                  type="button"
                  fullWidth
                  onClick={() =>
                    setRetryCount(
                      (count) => count + 1
                    )
                  }
                >
                  Try Again
                </Button>
              )}
            </div>
          )}

          {currentLinkVerified && (
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
                    setFullName(
                      event.target.value
                    )
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
                    setMobileNumber(
                      event.target.value
                    )
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
                    setEmail(
                      event.target.value
                    )
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
                    setPassword(
                      event.target.value
                    )
                  }
                  placeholder="Create a password"
                  autoComplete="new-password"
                  fullWidth
                  required
                />
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
                {loading
                  ? "Submitting..."
                  : "Submit Registration"}
              </Button>
            </form>
          )}
        </Card.Body>

        <Card.Footer>
          <p className="text-center text-sm text-[var(--color-text-secondary)]">
            Already have an account?{" "}

            <button
              type="button"
              onClick={() =>
                navigate(loginPath)
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
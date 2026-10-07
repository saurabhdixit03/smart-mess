import { useCallback, useEffect, useRef, useState } from "react";

import { paymentApi } from "../api";
import type { PaymentCheckout } from "../types";

interface CashfreeResult {
  error?: {
    message?: string;
  };
}

interface CashfreeClient {
  checkout(options: {
    paymentSessionId: string;
    redirectTarget: "_modal";
  }): Promise<CashfreeResult>;
}

type CashfreeFactory = (options: {
  mode: "sandbox" | "production";
}) => CashfreeClient;

type CashfreeWindow = Window & {
  Cashfree?: CashfreeFactory;
};

let sdkPromise: Promise<CashfreeFactory> | null = null;

function loadCashfree(): Promise<CashfreeFactory> {
  const cashfreeWindow = window as CashfreeWindow;

  if (cashfreeWindow.Cashfree) {
    return Promise.resolve(cashfreeWindow.Cashfree);
  }

  if (sdkPromise) {
    return sdkPromise;
  }

  sdkPromise = new Promise<CashfreeFactory>((resolve, reject) => {
    const script = document.createElement("script");

    script.src = "https://sdk.cashfree.com/js/v3/cashfree.js";
    script.async = true;

    const timeout = window.setTimeout(() => {
      fail();
    }, 15000);

    function fail() {
      window.clearTimeout(timeout);
      script.remove();
      sdkPromise = null;

      reject(
        new Error(
          "Payment checkout could not load. Check your connection and try again."
        )
      );
    }

    script.onerror = fail;

    script.onload = () => {
      window.clearTimeout(timeout);

      if (!cashfreeWindow.Cashfree) {
        fail();
        return;
      }

      resolve(cashfreeWindow.Cashfree);
    };

    document.head.appendChild(script);
  });

  return sdkPromise;
}

function getErrorMessage(error: unknown): string {
  return error instanceof Error
    ? error.message
    : "Payment could not be checked. Please try again.";
}

export function usePaymentCheckout() {
  const [payment, setPayment] = useState<PaymentCheckout | null>(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const generation = useRef(0);
  const busy = useRef(false);

  useEffect(() => {
    return () => {
      generation.current += 1;
    };
  }, []);

  const reset = useCallback(() => {
    generation.current += 1;
    setPayment(null);
    setLoading(false);
    setSubmitting(false);
    setError(null);
  }, []);

  const fetchCheckout = useCallback(async (billId: number) => {
    const currentGeneration = ++generation.current;

    setPayment(null);
    setError(null);
    setLoading(true);

    try {
      const response = await paymentApi.createCheckout(billId);

      if (generation.current !== currentGeneration) {
        return;
      }

      setPayment(response.data);
    } catch (err) {
      if (generation.current === currentGeneration) {
        setError(getErrorMessage(err));
      }
    } finally {
      if (generation.current === currentGeneration) {
        setLoading(false);
      }
    }
  }, []);

  const verifyPayment = useCallback(async () => {
    if (!payment || loading || busy.current) {
      return null;
    }

    const currentGeneration = generation.current;

    busy.current = true;
    setSubmitting(true);
    setError(null);

    try {
      const response = await paymentApi.verifyCheckout(
        payment.paymentOrderId
      );

      if (generation.current !== currentGeneration) {
        return null;
      }

      setPayment(response.data);

      return response.data;
    } catch (err) {
      if (generation.current === currentGeneration) {
        setError(getErrorMessage(err));
      }

      return null;
    } finally {
      busy.current = false;

      if (generation.current === currentGeneration) {
        setSubmitting(false);
      }
    }
  }, [payment, loading]);

  const openCheckout = useCallback(async () => {
    if (
      !payment ||
      payment.status !== "ACTIVE" ||
      !payment.paymentSessionId ||
      loading ||
      busy.current
    ) {
      return null;
    }

    const currentGeneration = generation.current;

    busy.current = true;
    setSubmitting(true);
    setError(null);

    try {
      const factory = await loadCashfree();

      if (generation.current !== currentGeneration) {
        return null;
      }

      const cashfree = factory({
        mode:
          payment.environment === "SANDBOX"
            ? "sandbox"
            : "production",
      });

      const result = await cashfree.checkout({
        paymentSessionId: payment.paymentSessionId,
        redirectTarget: "_modal",
      });

      if (generation.current !== currentGeneration) {
        return null;
      }

      // The backend determines payment status, even if checkout
      // closes or reports an error.
      const response = await paymentApi.verifyCheckout(
        payment.paymentOrderId
      );

      if (generation.current !== currentGeneration) {
        return null;
      }

      setPayment(response.data);

      if (result?.error && response.data.status !== "PAID") {
        setError(
          result.error.message ||
            "Checkout did not complete. You can check payment status again."
        );
      }

      return response.data;
    } catch (err) {
      if (generation.current === currentGeneration) {
        setError(getErrorMessage(err));
      }

      return null;
    } finally {
      busy.current = false;

      if (generation.current === currentGeneration) {
        setSubmitting(false);
      }
    }
  }, [payment, loading]);

  return {
    payment,
    loading,
    submitting,
    error,
    fetchCheckout,
    verifyPayment,
    openCheckout,
    reset,
  };
}
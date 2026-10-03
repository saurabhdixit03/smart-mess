import type {
  ApiResponse,
  MessRegistrationInfoResponse,
} from "../types/auth.types";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL;

/*
 * Registration links are public.
 *
 * Do not attach an existing account's JWT or apply
 * authenticated-session redirects to this lookup.
 */
export async function getMessRegistrationInfo(
  registrationCode: string,
  signal?: AbortSignal
): Promise<MessRegistrationInfoResponse> {
  const endpoint =
    "/public/messes/registration/" +
    encodeURIComponent(registrationCode);

  const response = await fetch(
    `${API_BASE_URL}${endpoint}`,
    {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
      signal,
    }
  );

  if (response.status === 404) {
    throw new Error(
      "This registration link is invalid. Please ask your mess owner for the correct link."
    );
  }

  if (!response.ok) {
    throw new Error(
      "Unable to verify this registration link. Please try again."
    );
  }

  const result: ApiResponse<MessRegistrationInfoResponse> =
    await response.json();

  if (
    !result.success ||
    typeof result.data?.messName !== "string" ||
    !result.data.messName.trim()
  ) {
    throw new Error(
      "Unable to load the mess details. Please try again."
    );
  }

  return result.data;
}
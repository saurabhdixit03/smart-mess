import type {
  CustomerLoginResponse,
  OwnerLoginResponse,
} from "../types/auth.types";

export type AuthRole = "OWNER" | "CUSTOMER";

const AUTH_TOKEN_KEY = "smart_mess_access_token";
const AUTH_ROLE_KEY = "smart_mess_user_role";
const OWNER_KEY = "smart_mess_owner";
const CUSTOMER_KEY = "smart_mess_customer";

export function saveOwnerAuthSession(
  loginResponse: OwnerLoginResponse
): void {
  localStorage.setItem(
    AUTH_TOKEN_KEY,
    loginResponse.accessToken
  );

  localStorage.setItem(
    AUTH_ROLE_KEY,
    "OWNER"
  );

  localStorage.setItem(
    OWNER_KEY,
    JSON.stringify({
      messOwnerId: loginResponse.messOwnerId,
      fullName: loginResponse.fullName,
      messName: loginResponse.messName,
    })
  );

  localStorage.removeItem(CUSTOMER_KEY);
}

export function saveCustomerAuthSession(
  loginResponse: CustomerLoginResponse
): void {
  localStorage.setItem(
    AUTH_TOKEN_KEY,
    loginResponse.accessToken
  );

  localStorage.setItem(
    AUTH_ROLE_KEY,
    "CUSTOMER"
  );

  localStorage.setItem(
    CUSTOMER_KEY,
    JSON.stringify({
      customerId: loginResponse.customerId,
      fullName: loginResponse.fullName,
      mobileNumber: loginResponse.mobileNumber,
    })
  );

  localStorage.removeItem(OWNER_KEY);
}

export function getAccessToken(): string | null {
  return localStorage.getItem(AUTH_TOKEN_KEY);
}

export function getAuthRole(): AuthRole | null {
  const role = localStorage.getItem(AUTH_ROLE_KEY);

  if (role === "OWNER" || role === "CUSTOMER") {
    return role;
  }

  return null;
}

export function getOwner(): {
  messOwnerId: number;
  fullName: string;
  messName: string;
} | null {
  const owner = localStorage.getItem(OWNER_KEY);

  if (!owner) {
    return null;
  }

  return JSON.parse(owner);
}

export function getCustomer(): {
  customerId: number;
  fullName: string;
  mobileNumber: string;
} | null {
  const customer = localStorage.getItem(CUSTOMER_KEY);

  if (!customer) {
    return null;
  }

  return JSON.parse(customer);
}

export function clearAuthSession(): void {
  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(AUTH_ROLE_KEY);
  localStorage.removeItem(OWNER_KEY);
  localStorage.removeItem(CUSTOMER_KEY);
}

export function isAuthenticated(): boolean {
  return Boolean(getAccessToken());
}
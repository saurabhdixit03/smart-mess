export interface OwnerLoginRequest {
  email: string;
  password: string;
}

export interface OwnerLoginResponse {
  accessToken: string;
  tokenType: string;
  messOwnerId: number;
  fullName: string;
  messName: string;
  messId: number;
}

export interface ApiResponse<T> {
  timestamp: string;
  success: boolean;
  message: string;
  path: string;
  data: T;
}

export interface OwnerRegistrationRequest {
  fullName: string;
  messName: string;
  mobileNumber: string;
  email: string;
  password: string;
}

export interface CustomerLoginRequest {
  email: string;
  password: string;
}

export interface CustomerLoginResponse {
  accessToken: string;
  tokenType: string;
  customerId: number;
  fullName: string;
  mobileNumber: string;
  messName: string;
}

export interface CustomerRegistrationRequest {
  fullName: string;
  mobileNumber: string;
  email: string;
  password: string;
  registrationCode: string;
}

/*
 * Registration confirms a pending account.
 * An access token is issued only after approval and login.
 */
export interface CustomerRegistrationResponse {
  customerId: number;
  fullName: string;
  mobileNumber: string;
  status: "PENDING";
}

export interface MessRegistrationInfoResponse {
  messName: string;
}

export type AuthRole = "OWNER" | "CUSTOMER";

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}
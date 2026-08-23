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

// customer login 

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
}

export interface CustomerRegistrationRequest {
  fullName: string;
  mobileNumber: string;
  email: string;
  password: string;
}

// for Forget and Reset password

export type AuthRole =
  | "OWNER"
  | "CUSTOMER";

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}
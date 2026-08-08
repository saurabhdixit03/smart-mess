export interface OwnerLoginRequest {
mobileNumber: string;
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
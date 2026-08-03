export type CustomerProfile = {
  customerId: number;

  fullName: string;

  mobileNumber: string;

  email: string;

  joiningDate: string;

  status: "ACTIVE" | "INACTIVE";

  createdAt: string;

  updatedAt: string;
};

export type UpdateProfileRequest = {
  fullName: string;

  mobileNumber: string;

  email: string;
};

export type ApiResponse<T> = {
  success: boolean;

  message: string;

  path: string;

  timestamp: string;

  data: T;
};
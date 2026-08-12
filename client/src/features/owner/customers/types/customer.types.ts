
export interface UpdateCustomerRequest {
  remarks: string;
}

export type CustomerStatus =
  | "ACTIVE"
  | "INACTIVE";

export interface CustomerResponse {
  customerId: number;
  fullName: string;
  mobileNumber: string;
  email: string;
  remarks: string;
  joiningDate: string;
  status: CustomerStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  path: string;
  data: T;
  timestamp: string;
}

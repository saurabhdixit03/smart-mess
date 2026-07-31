export interface CreateMessSettingsRequest {
  upiId: string;
  receiverName: string;
}

export interface UpdateMessSettingsRequest {
  upiId: string;
  receiverName: string;
}

export interface MessSettingsResponse {
  settingsId: number;
  upiId: string;
  receiverName: string;
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
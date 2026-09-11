export type NotificationType =
  | "MENU_PUBLISHED";

export interface Notification {
  notificationId: number;
  customerId: number;
  notificationType: NotificationType;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  path: string;
  data: T;
}
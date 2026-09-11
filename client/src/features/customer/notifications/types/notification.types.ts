export type NotificationType =
  | "MENU_PUBLISHED"
  | "MESS_CLOSURE"
  | "WEEKLY_SCHEDULE"
  | "RESPONSE_WINDOW"
  | "MEAL_PRICING";

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
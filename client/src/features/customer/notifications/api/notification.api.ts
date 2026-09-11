import api from "@/lib/api";

import type {
  ApiResponse,
  Notification,
} from "../types/notification.types";

const NOTIFICATION_ENDPOINT = "/notifications";

export const notificationApi = {
  getNotifications() {
    return api.get<ApiResponse<Notification[]>>(
      NOTIFICATION_ENDPOINT
    );
  },

  getUnreadNotifications() {
    return api.get<ApiResponse<Notification[]>>(
      `${NOTIFICATION_ENDPOINT}/unread`
    );
  },

  getUnreadCount() {
    return api.get<ApiResponse<number>>(
      `${NOTIFICATION_ENDPOINT}/unread-count`
    );
  },

  markAsRead(notificationId: number) {
    return api.patch<ApiResponse<Notification>>(
      `${NOTIFICATION_ENDPOINT}/${notificationId}/read`
    );
  },

  markAllAsRead() {
    return api.patch<ApiResponse<null>>(
      `${NOTIFICATION_ENDPOINT}/read-all`
    );
  },
};
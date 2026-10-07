import {
  getAccessToken,
  clearAuthSession,
  getAuthRole,
} from "@/features/auth/utils/auth.utils";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

export class ApiError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

async function request<T>(
  endpoint: string,
  method: HttpMethod,
  body?: unknown
): Promise<T> {
  const token = getAccessToken();

  const headers: HeadersInit = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  const data = await response.json();

  if (!response.ok) {
    if (response.status === 401) {
      const role = getAuthRole();

      clearAuthSession();

      window.location.href =
        role === "OWNER"
          ? "/owner/login"
          : "/customer/login";

      throw new ApiError(
        "Session expired. Please login again.",
        response.status
      );
    }

    throw new ApiError(
      data.message ||
        (response.status === 403
          ? "You are not authorized to perform this action."
          : "Something went wrong."),
      response.status
    );
  }

  return data as T;
}

const api = {
  get<T>(endpoint: string) {
    return request<T>(endpoint, "GET");
  },

  post<T>(endpoint: string, body?: unknown) {
    return request<T>(endpoint, "POST", body);
  },

  put<T>(endpoint: string, body: unknown) {
    return request<T>(endpoint, "PUT", body);
  },

  patch<T>(endpoint: string, body?: unknown) {
    return request<T>(endpoint, "PATCH", body);
  },

  delete<T>(endpoint: string) {
    return request<T>(endpoint, "DELETE");
  },
};

export default api;
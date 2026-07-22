const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

type HttpMethod = "GET" | "POST" | "PUT" | "DELETE";

async function request<T>(
  endpoint: string,
  method: HttpMethod,
  body?: unknown
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method,
    headers: {
      "Content-Type": "application/json",
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || "Something went wrong");
  }

  return data as T;
}

const api = {
  get<T>(endpoint: string) {
    return request<T>(endpoint, "GET");
  },

  post<T>(endpoint: string, body: unknown) {
    return request<T>(endpoint, "POST", body);
  },

  put<T>(endpoint: string, body: unknown) {
    return request<T>(endpoint, "PUT", body);
  },

  delete<T>(endpoint: string) {
    return request<T>(endpoint, "DELETE");
  },
};

export default api;
export type AuthUser = {
  username: string;
  email: string | null;
  role: "STUDENT" | "LECTURER" | "ADMIN";
  fullName: string | null;
  institutionalId: string | null;
};

async function csrfToken() {
  const response = await fetch("/api/auth/csrf", { credentials: "same-origin", cache: "no-store" });
  const token = response.headers.get("X-CSRF-TOKEN");
  if (!response.ok || !token) throw new Error("Could not initialize a secure request.");
  return token;
}

async function request<T>(path: string, body?: unknown, method = "GET"): Promise<T> {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (method !== "GET") headers["X-CSRF-TOKEN"] = await csrfToken();
  const response = await fetch(path, {
    method, credentials: "same-origin", cache: "no-store", headers,
    ...(body === undefined ? {} : { body: JSON.stringify(body) }),
  });
  if (!response.ok) {
    const data = await response.json().catch(() => null);
    throw new Error(data?.detail || data?.message || (response.status === 401 ? "Invalid username or password." : `Request failed (${response.status}).`));
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export const login = (username: string, password: string) => request<AuthUser>("/api/auth/login", { username, password }, "POST");
export const register = (role: "student" | "lecturer", details: Record<string, string>) => request<AuthUser>(`/api/auth/register/${role}`, details, "POST");
export const getCurrentUser = () => request<AuthUser>("/api/auth/me");
export const logout = () => request<void>("/api/auth/logout", undefined, "POST");

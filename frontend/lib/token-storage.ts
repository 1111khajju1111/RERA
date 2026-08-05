/**
 * Dedicated storage for the bearer auth token — deliberately separate
 * from the Zustand auth-store's persisted blob so lib/api.ts can read it
 * without importing from store/auth-store.ts (which itself imports
 * authApi from lib/api.ts — importing the other way would create a
 * circular dependency).
 */

const TOKEN_KEY = "rera_auth_token";

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  if (typeof window === "undefined") return;
  window.localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(TOKEN_KEY);
}

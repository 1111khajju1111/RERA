import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { UserResponse } from "@/lib/types";
import { authApi } from "@/lib/api";

interface AuthState {
  user: UserResponse | null;
  isLoading: boolean;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

// Persisted to localStorage purely so the UI can optimistically show the
// logged-in name without a round trip on page load. It does NOT replace
// the session cookie for actual auth — every real request still relies
// on the httpOnly JSESSIONID cookie sent by the browser automatically.
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isLoading: false,
      error: null,

      login: async (email, password) => {
        set({ isLoading: true, error: null });
        try {
          const user = await authApi.login({ email, password });
          set({ user, isLoading: false });
        } catch (err: any) {
          set({ error: err.message || "Login failed", isLoading: false });
          throw err;
        }
      },

      register: async (name, email, password) => {
        set({ isLoading: true, error: null });
        try {
          await authApi.register({ name, email, password });
          const user = await authApi.login({ email, password });
          set({ user, isLoading: false });
        } catch (err: any) {
          set({ error: err.message || "Registration failed", isLoading: false });
          throw err;
        }
      },

      logout: async () => {
        await authApi.logout().catch(() => {});
        set({ user: null });
      },

      clearError: () => set({ error: null }),
    }),
    { name: "rera-auth-storage" }
  )
);

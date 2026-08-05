import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { UserResponse } from "@/lib/types";
import { authApi } from "@/lib/api";
import { setToken, clearToken } from "@/lib/token-storage";

interface AuthState {
  user: UserResponse | null;
  isLoading: boolean;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

// `user` here is persisted purely so the UI can optimistically show the
// logged-in name/avoid a flash-redirect on refresh — it is NOT the
// mechanism that authenticates API calls. The actual bearer token lives
// separately in lib/token-storage.ts (see api.ts's authHeaders()).
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isLoading: false,
      error: null,

      login: async (email, password) => {
        set({ isLoading: true, error: null });
        try {
          const { user, token } = await authApi.login({ email, password });
          setToken(token);
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
          const { user, token } = await authApi.login({ email, password });
          setToken(token);
          set({ user, isLoading: false });
        } catch (err: any) {
          set({ error: err.message || "Registration failed", isLoading: false });
          throw err;
        }
      },

      logout: async () => {
        await authApi.logout().catch(() => {});
        clearToken();
        set({ user: null });
      },

      clearError: () => set({ error: null }),
    }),
    { name: "rera-auth-storage" }
  )
);

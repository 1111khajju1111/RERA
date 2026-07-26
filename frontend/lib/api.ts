/**
 * Thin fetch wrapper for the Spring Boot backend.
 *
 * Auth: bearer token (Authorization header), NOT cookies. Originally this
 * used session cookies + credentials:"include", but with the frontend
 * (Vercel) and backend (Render) on unrelated domains, browsers classify
 * that cookie as third-party — Safari/Firefox/Brave block it
 * unconditionally, Chrome blocks it in Incognito and for privacy-toggled
 * users. A token in a normal header isn't a cookie, so it isn't subject
 * to any cookie policy anywhere. See lib/token-storage.ts for where the
 * token itself lives.
 */

import type {
  UserResponse, ProjectResponse, BuildingResponse, ViolationResponse,
  ComplianceSummaryResponse, ChatMessageResponse, ApiErrorResponse, SiteAnalysisResponse,
  ComplianceHistoryEntry,
} from "./types";
import { getToken } from "./token-storage";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export class ApiError extends Error {
  status: number;
  details: string[] | null;
  constructor(body: ApiErrorResponse) {
    super(body.message);
    this.status = body.status;
    this.details = body.details;
  }
}

function authHeaders(): Record<string, string> {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...authHeaders(),
      ...options.headers,
    },
  });

  if (!res.ok) {
    let body: ApiErrorResponse;
    try {
      body = await res.json();
    } catch {
      throw new Error(`Request failed: ${res.status} ${res.statusText}`);
    }
    throw new ApiError(body);
  }

  if (res.status === 204) return undefined as T;
  return res.json();
}

// ---- Auth ----
export interface AuthResponse {
  user: UserResponse;
  token: string;
}

export const authApi = {
  register: (data: { name: string; email: string; password: string }) =>
    request<UserResponse>("/api/auth/register", { method: "POST", body: JSON.stringify(data) }),

  login: (data: { email: string; password: string }) =>
    request<AuthResponse>("/api/auth/login", { method: "POST", body: JSON.stringify(data) }),

  logout: () => request<void>("/api/auth/logout", { method: "POST" }),
};

// ---- Projects ----
export const projectApi = {
  list: () => request<ProjectResponse[]>("/api/projects"),

  get: (id: number) => request<ProjectResponse>(`/api/projects/${id}`),

  create: (data: { name: string; description?: string; location?: string; plotAreaSqm?: number }) =>
    request<ProjectResponse>("/api/projects", { method: "POST", body: JSON.stringify(data) }),

  remove: (id: number) => request<void>(`/api/projects/${id}`, { method: "DELETE" }),
};

// ---- Buildings ----
export const buildingApi = {
  listForProject: (projectId: number) =>
    request<BuildingResponse[]>(`/api/projects/${projectId}/buildings`),
};

// ---- Compliance ----
export const complianceApi = {
  getSummary: (projectId: number) =>
    request<ComplianceSummaryResponse>(`/api/projects/${projectId}/compliance-score`),

  getHistory: (projectId: number) =>
    request<ComplianceHistoryEntry[]>(`/api/projects/${projectId}/compliance-history`),
};

// ---- Violations ----
export const violationApi = {
  listForProject: (projectId: number) =>
    request<ViolationResponse[]>(`/api/projects/${projectId}/violations`),

  updateStatus: (projectId: number, violationId: number, status: string, note?: string) =>
    request<ViolationResponse>(`/api/projects/${projectId}/violations/${violationId}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status, note }),
    }),
};

// ---- Upload (multipart — bypasses the JSON `request` helper) ----
export async function uploadFile(projectId: number, file: File) {
  const formData = new FormData();
  formData.append("file", file);

  const res = await fetch(`${BASE_URL}/api/projects/${projectId}/upload`, {
    method: "POST",
    headers: { ...authHeaders() }, // no Content-Type here — the browser sets the multipart boundary itself
    body: formData,
  });

  if (!res.ok) {
    const body = await res.json();
    throw new ApiError(body);
  }
  return res.json();
}

// ---- GIS ----
export const gisApi = {
  analyze: (projectId: number, address: string) =>
    request<SiteAnalysisResponse>(`/api/projects/${projectId}/gis/analyze`, {
      method: "POST",
      body: JSON.stringify({ address }),
    }),

  get: (projectId: number) =>
    request<SiteAnalysisResponse>(`/api/projects/${projectId}/gis`),
};

// ---- Reports ----
export interface AuditReportResponse {
  id: number;
  complianceScore: number | null;
  approvalProbability: number | null;
  format: string;
  downloadable: boolean;
  generatedAt: string;
}

export const reportApi = {
  generate: (projectId: number, format: "PDF" | "DOCX" | "XLSX") =>
    request<AuditReportResponse>(`/api/projects/${projectId}/reports`, {
      method: "POST",
      body: JSON.stringify({ format }),
    }),

  list: (projectId: number) =>
    request<AuditReportResponse[]>(`/api/projects/${projectId}/reports`),

  // A real file download, not JSON — since auth is now a header (not a
  // cookie the browser attaches automatically), a plain <a>/window.open
  // link WON'T carry the Authorization header. downloadUrl() alone is no
  // longer sufficient — see the reports page's download handler, which
  // fetches the file as a blob (with the header attached) and saves it
  // via an object URL instead of navigating directly to this URL.
  downloadUrl: (projectId: number, reportId: number) =>
    `${BASE_URL}/api/projects/${projectId}/reports/${reportId}/download`,

  downloadBlob: async (projectId: number, reportId: number): Promise<Blob> => {
    const res = await fetch(`${BASE_URL}/api/projects/${projectId}/reports/${reportId}/download`, {
      headers: { ...authHeaders() },
    });
    if (!res.ok) throw new Error(`Download failed: ${res.status}`);
    return res.blob();
  },
};

// ---- Chat ----
export const chatApi = {
  history: (projectId: number) =>
    request<ChatMessageResponse[]>(`/api/projects/${projectId}/chat`),

  send: (projectId: number, message: string) =>
    request<ChatMessageResponse>(`/api/projects/${projectId}/chat`, {
      method: "POST",
      body: JSON.stringify({ message }),
    }),
};

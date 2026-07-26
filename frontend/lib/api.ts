/**
 * Thin fetch wrapper for the Spring Boot backend. Sends credentials
 * (cookies) on every request since auth is session-based (Phase 3
 * SecurityConfig), not JWT — so there's no token to attach manually.
 */

import type {
  UserResponse, ProjectResponse, BuildingResponse, ViolationResponse,
  ComplianceSummaryResponse, ChatMessageResponse, ApiErrorResponse, SiteAnalysisResponse,
  ComplianceHistoryEntry,
} from "./types";

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

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
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
export const authApi = {
  register: (data: { name: string; email: string; password: string }) =>
    request<UserResponse>("/api/auth/register", { method: "POST", body: JSON.stringify(data) }),

  login: (data: { email: string; password: string }) =>
    request<UserResponse>("/api/auth/login", { method: "POST", body: JSON.stringify(data) }),

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
    credentials: "include",
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

  // Not a JSON request — returns a real file download, so this builds the
  // URL for a direct browser navigation/anchor rather than using `request()`.
  downloadUrl: (projectId: number, reportId: number) =>
    `${BASE_URL}/api/projects/${projectId}/reports/${reportId}/download`,
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

// auth.model.ts

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

// Shared by both login AND register, since register auto-logs-in
export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresAt: number;
}

export interface ApiErrorResponse {
  message: string;
  status: number;
  error: string;
  path: string;
  timestamp: string;
  fieldErrors?: Record<string, string>;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
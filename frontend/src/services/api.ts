import {
  Problem,
  InterviewSession,
  QuestionGenerationRequest,
  QuestionGenerationResponse,
  User,
  RegisterPayload,
  LoginPayload,
  AuthResponse,
  StartInterviewPayload,
  SubmitInterviewPayload,
  Interview,
} from '../types';

// Spring Boot REST API Base URL
// In development: defaults to http://localhost:8080/api
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const TOKEN_KEY = 'codementor_auth_token';
const USER_KEY = 'codementor_auth_user';

export const authStorage = {
  getToken(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  },
  getUser(): User | null {
    try {
      const data = localStorage.getItem(USER_KEY);
      return data ? JSON.parse(data) : null;
    } catch {
      return null;
    }
  },
  setAuth(token: string, user: User): void {
    try {
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    } catch (e) {
      console.error('Failed to save auth to storage', e);
    }
  },
  clearAuth(): void {
    try {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    } catch (e) {
      console.error('Failed to clear auth from storage', e);
    }
  },
};

function getAuthHeaders(): HeadersInit {
  const token = authStorage.getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

export interface HealthStatus {
  status: string;
  service: string;
  database?: string;
  timestamp?: string;
  version?: string;
}

export const api = {
  async register(payload: RegisterPayload): Promise<AuthResponse> {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      const message = data.details?.email || data.details?.password || data.details?.name || data.message || 'Registration failed';
      throw new Error(message);
    }
    authStorage.setAuth(data.token, data.user);
    return data;
  },

  async login(payload: LoginPayload): Promise<AuthResponse> {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      const message = data.details?.email || data.details?.password || data.message || 'Invalid email or password';
      throw new Error(message);
    }
    authStorage.setAuth(data.token, data.user);
    return data;
  },

  async getCurrentUser(): Promise<User | null> {
    const token = authStorage.getToken();
    if (!token) return null;
    try {
      const res = await fetch(`${API_BASE}/auth/me`, {
        headers: getAuthHeaders(),
      });
      if (!res.ok) {
        authStorage.clearAuth();
        return null;
      }
      return await res.json();
    } catch {
      return null;
    }
  },

  async getHealth(): Promise<HealthStatus> {
    try {
      const res = await fetch(`${API_BASE}/health`);
      if (!res.ok) throw new Error(`Health check failed: ${res.statusText}`);
      return await res.json();
    } catch (err) {
      console.warn('Spring Boot /api/health endpoint unreachable at ' + API_BASE, err);
      return { status: 'DISCONNECTED', service: 'CodeMentor AI (Spring Boot 8080)', database: 'UNAVAILABLE' };
    }
  },

  async getProblems(): Promise<Problem[]> {
    const res = await fetch(`${API_BASE}/problems`);
    if (!res.ok) throw new Error('Failed to fetch problems from Spring Boot');
    return await res.json();
  },

  async getProblemById(id: string): Promise<Problem> {
    const res = await fetch(`${API_BASE}/problems/${id}`);
    if (!res.ok) throw new Error(`Problem not found: ${id}`);
    return await res.json();
  },

  // V1.3 AI Coding Interview Engine API Methods
  async startInterview(payload: StartInterviewPayload): Promise<Interview> {
    const res = await fetch(`${API_BASE}/interviews/start`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload),
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      throw new Error(data.message || 'Failed to start AI coding interview');
    }
    return data;
  },

  async submitInterview(id: number, payload: SubmitInterviewPayload): Promise<Interview> {
    const res = await fetch(`${API_BASE}/interviews/${id}/submit`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload),
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      throw new Error(data.message || 'Failed to evaluate interview solution');
    }
    return data;
  },

  async getInterviewHistory(): Promise<Interview[]> {
    const res = await fetch(`${API_BASE}/interviews/history`, {
      headers: getAuthHeaders(),
    });
    if (!res.ok) {
      throw new Error('Failed to load interview history');
    }
    return await res.json();
  },

  async getInterviewById(id: number): Promise<Interview> {
    const res = await fetch(`${API_BASE}/interviews/${id}`, {
      headers: getAuthHeaders(),
    });
    if (!res.ok) {
      throw new Error('Failed to load interview details');
    }
    return await res.json();
  },

  async getInterviews(): Promise<InterviewSession[]> {
    const res = await fetch(`${API_BASE}/interviews`, {
      headers: getAuthHeaders(),
    });
    if (!res.ok) throw new Error('Failed to fetch interviews from Spring Boot');
    return await res.json();
  },

  async createInterview(payload: Partial<InterviewSession>): Promise<InterviewSession> {
    const res = await fetch(`${API_BASE}/interviews`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const errBody = await res.json().catch(() => ({}));
      throw new Error(errBody.message || 'Failed to create interview session in Spring Boot');
    }
    return await res.json();
  },

  async generateQuestion(req: QuestionGenerationRequest): Promise<QuestionGenerationResponse> {
    const res = await fetch(`${API_BASE}/ai/generate-question`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(req),
    });
    if (!res.ok) {
      const errBody = await res.json().catch(() => ({}));
      throw new Error(errBody.message || 'Failed to generate question via Spring Boot Gemini Service');
    }
    return await res.json();
  },
};

import {
  Problem,
  InterviewSession,
  QuestionGenerationRequest,
  QuestionGenerationResponse,
} from '../types';

// Spring Boot REST API Base URL
// In development: defaults to http://localhost:8080/api
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export interface HealthStatus {
  status: string;
  service: string;
  database?: string;
  timestamp?: string;
  version?: string;
}

export const api = {
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

  async getInterviews(): Promise<InterviewSession[]> {
    const res = await fetch(`${API_BASE}/interviews`);
    if (!res.ok) throw new Error('Failed to fetch interviews from Spring Boot');
    return await res.json();
  },

  async createInterview(payload: Partial<InterviewSession>): Promise<InterviewSession> {
    const res = await fetch(`${API_BASE}/interviews`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
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
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    });
    if (!res.ok) {
      const errBody = await res.json().catch(() => ({}));
      throw new Error(errBody.message || 'Failed to generate question via Spring Boot Gemini Service');
    }
    return await res.json();
  },
};

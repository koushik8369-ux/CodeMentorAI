# CodeMentor AI

> **AI-Powered Coding Interview Preparation Platform for Software Engineering Candidates**

CodeMentor AI empowers software engineers to conquer technical interviews through targeted coding challenges, real-time code evaluation, algorithmic complexity analysis, multi-round technical mock interviews, and Gemini-powered personalized learning paths.

---

## 🏗 Architecture (Database-Free / Stateless Backend)

```
┌────────────────────────────────────────────────────────┐
│               React 19 + TypeScript Frontend           │
│                                                        │
│  - Browser localStorage (Interviews, Mocks, Analytics) │
│  - Local Candidate Profile Mode                        │
│  - Real-time Monaco-style Code Workspace               │
└───────────────────────────┬────────────────────────────┘
                            │ REST API (JSON / Bearer Token)
                            ▼
┌────────────────────────────────────────────────────────┐
│          Spring Boot 3.3.4 Backend (Port 8080)         │
│                                                        │
│  - Stateless AI Orchestration & Evaluation Engine      │
│  - Zero Database Dependency (No PostgreSQL/JPA needed) │
│  - Protected Gemini API Gateway                        │
└───────────────────────────┬────────────────────────────┘
                            │ HTTPS (Server-Side Only)
                            ▼
┌────────────────────────────────────────────────────────┐
│                   Google Gemini API                    │
│                  (gemini-3.8-flash)                    │
└────────────────────────────────────────────────────────┘
```

There is **only one backend**: Java 21 + Spring Boot running statelessly on port 8080.
The frontend uses Vite on port 3000/5173 and persists session data inside the browser's private local storage.

---

## 🛠 Tech Stack

- **Frontend**: React 19, Vite, TypeScript, Tailwind CSS, React Router 7, Lucide React
- **Client Storage**: Typed, versioned browser `localStorage` service with graceful error handling and corruption recovery
- **Backend**: Java 21, Spring Boot 3.3.4, Spring Web, Spring Security, Bean Validation, JJWT
- **Persistence**: Database-Free (Zero PostgreSQL, MySQL, MongoDB, or external database requirement)
- **AI Integration**: Google Gemini API (`gemini-3.8-flash`), isolated exclusively to the Spring Boot server

---

## 🚀 Getting Started

### 1. Environment Configuration

Copy `.env.example` to `.env`:

```bash
cp .env.example .env
```

#### Environment Variables

```ini
# Google Gemini API Key (Required for live AI evaluations)
GEMINI_API_KEY=YOUR_GEMINI_API_KEY

# Security & JWT Token Signing Secret (defaults to built-in secure key)
JWT_SECRET=CodeMentorAISecretKeyForJwtTokenGeneration2026Secure256BitsMinimum

# Frontend API Base URL (Pointing to Spring Boot REST backend)
VITE_API_BASE_URL=http://localhost:8080/api
```

*Note: No database credentials (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) are required.*

---

### 2. Running the Spring Boot Backend

The backend is located in `backend/`:

```bash
cd backend
mvn spring-boot:run
```

The Spring Boot REST API starts on `http://localhost:8080`.

Verify backend health:

```bash
curl http://localhost:8080/api/health
```

Expected response:
```json
{
  "status": "UP",
  "service": "CodeMentor AI",
  "database": "Stateless AI Engine (Zero Database)"
}
```

Run backend unit and integration test suite:

```bash
mvn test
```

---

### 3. Running the Frontend

The React frontend is served via Vite:

```bash
npm install
npm run dev
```

Open the application at `http://localhost:3000` (or `http://localhost:5173`).

---

## 📡 REST API Specifications

All API endpoints are hosted exclusively by Spring Boot on port 8080:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Backend status (Stateless AI Engine) |
| `POST` | `/api/auth/login` | Candidate demo session token issuer |
| `POST` | `/api/auth/register` | Local candidate profile registration |
| `GET` | `/api/auth/me` | Current authenticated candidate profile |
| `GET` | `/api/problems` | List curated algorithmic interview questions |
| `GET` | `/api/problems/{id}` | Problem details, constraints, test cases, and starter code |
| `POST` | `/api/interviews/start` | Initialize an AI coding interview session |
| `POST` | `/api/interviews/{id}/submit` | AI evaluation of code correctness, complexity, and quality |
| `GET` | `/api/interviews/history` | Candidate interview history |
| `POST` | `/api/mock-interviews/start` | Start multi-round technical/behavioral mock interview |
| `POST` | `/api/mock-interviews/{id}/answer` | Submit mock interview answer & receive AI evaluation and follow-up |
| `GET` | `/api/analytics/overview` | Performance metrics overview |
| `GET` | `/api/analytics/ai-insights` | Gemini personalized strategic improvement plan |

---

## 🔒 Security Principles

- **Zero Database Exposure**: User solutions and session records stay within the candidate's browser storage.
- **Server-Side API Key Isolation**: `GEMINI_API_KEY` is kept strictly on the Spring Boot server; never leaked to frontend client or logs.
- **CORS Configured**: Spring Boot allows cross-origin requests from the frontend client development server.
- **Zero Pill / Anti-Slop UI**: Clean typographical layout adhering to modern software engineering design standards.

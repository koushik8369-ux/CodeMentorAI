# CodeMentor AI

> **AI-Powered Coding Interview Preparation Platform for Software Engineering Candidates**

CodeMentor AI empowers software engineers to conquer technical interviews through targeted coding challenges, real-time code evaluation, algorithmic complexity analysis, and Gemini-powered interview question generation.

---

## 🏗 Architecture

```
React + Vite + TypeScript
        ↓
Spring Boot REST API (Port 8080)
        ↓
PostgreSQL / Supabase PostgreSQL

Spring Boot
        ↓
Google Gemini API
```

There is **only one backend**: Java 21 + Spring Boot.
Vite serves purely as the frontend client development server.

---

## 🛠 Tech Stack

- **Frontend**: React 19, Vite, TypeScript, Tailwind CSS, React Router, Lucide React
- **Backend**: Java 21, Spring Boot 3.3.4, Spring Web, Spring Data JPA, Hibernate, Bean Validation
- **Database**: PostgreSQL / Supabase PostgreSQL
- **AI Integration**: Google Gemini API (isolated in Spring Boot `GeminiService`)

---

## 🚀 Getting Started

### 1. Environment Configuration

Copy `.env.example` to `.env`:

```bash
cp .env.example .env
```

Configuration variables:

```ini
VITE_API_BASE_URL=http://localhost:8080/api
GEMINI_API_KEY=your_gemini_api_key
DB_URL=jdbc:postgresql://localhost:5432/codementor_db
DB_USERNAME=postgres
DB_PASSWORD=your_password
```

### 2. Running the Spring Boot Backend

The backend is located in `backend/`:

```bash
cd backend
mvn spring-boot:run
```

The Spring Boot REST API will start on:

```text
http://localhost:8080
```

Verify backend health:

```bash
curl http://localhost:8080/api/health
```

### 3. Running the Frontend

The React frontend is served via Vite:

```bash
npm install
npm run dev
```

Open the application at:

```text
http://localhost:5173
```

---

## 📡 REST API Specifications

All endpoints are hosted exclusively by Spring Boot on port 8080:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Backend status and real database connectivity check |
| `GET` | `/api/problems` | List all curated interview questions |
| `GET` | `/api/problems/{id}` | Problem statement, test cases, and starter code |
| `GET` | `/api/interviews` | Candidate mock interview sessions |
| `POST` | `/api/interviews` | Initialize a new interview session |
| `POST` | `/api/ai/generate-question` | Generate an AI interview question tailored to role & topic |

---

## 🔒 Security Principles

- **No Secrets in Client**: `GEMINI_API_KEY` and database credentials exist strictly on the Spring Boot server.
- **Direct Backend Communication**: React communicates only with Spring Boot REST endpoints.
- **CORS Configured**: Spring Boot allows cross-origin requests from `http://localhost:5173`.
- **Server-Side Validation**: All incoming requests are strictly validated using `@Valid`.

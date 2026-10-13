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

#### Required Environment Variables

Configure the following variables in your environment or `.env` file (these are placeholders only; never commit real credentials):

```ini
DB_URL=jdbc:postgresql://HOST:5432/DATABASE?sslmode=require
DB_USERNAME=YOUR_DATABASE_USERNAME
DB_PASSWORD=YOUR_DATABASE_PASSWORD
JWT_SECRET=YOUR_SECURE_SECRET
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
VITE_API_BASE_URL=http://localhost:8080/api
```

#### Connecting a Hosted PostgreSQL Database (Neon / Supabase / Cloud SQL)

CodeMentor AI supports any standard PostgreSQL 14+ database, including hosted serverless providers:

1. **Neon (Serverless Postgres)**:
   - Create a project on [Neon](https://neon.tech).
   - In the dashboard, copy the JDBC connection string or build it using the host, database, user, and password.
   - Append `?sslmode=require` to enable mandatory SSL encryption:
     ```ini
     DB_URL=jdbc:postgresql://ep-example-123456.us-east-2.aws.neon.tech:5432/neondb?sslmode=require
     DB_USERNAME=YOUR_NEON_USERNAME
     DB_PASSWORD=YOUR_NEON_PASSWORD
     ```

2. **Supabase**:
   - In your Supabase project settings under **Database** -> **Connection String** -> **JDBC**, copy the host and database details:
     ```ini
     DB_URL=jdbc:postgresql://db.YOUR_PROJECT_REF.supabase.co:5432/postgres?sslmode=require
     DB_USERNAME=postgres
     DB_PASSWORD=YOUR_SUPABASE_DB_PASSWORD
     ```

3. **Data Safety & Schema Updates**:
   - CodeMentor AI uses `spring.jpa.hibernate.ddl-auto=update`.
   - Existing tables and records are **never** dropped, truncated, or deleted. Hibernate adds missing columns and tables non-destructively.
   - For local development with a local PostgreSQL server or Docker container, the default fallback is `jdbc:postgresql://localhost:5432/codementor_db`.

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

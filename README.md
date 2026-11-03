# CodeMentor AI

> **AI-Powered Coding Interview Preparation Platform for Software Engineering Candidates**

CodeMentor AI empowers software engineers to conquer technical interviews through targeted coding challenges, real-time code evaluation, algorithmic complexity analysis, multi-round technical mock interviews, and Gemini-powered personalized learning paths.

---

## 📸 Screenshots & Product Walkthrough

*(Screenshots will be captured and linked here following initial public production deployment)*

| View | Description |
|---|---|
| **Candidate Dashboard** | Real-time interview metrics, recent history, readiness score, and quick track actions |
| **Coding Workspace** | Split-pane problem specifications, multi-language editor, and Gemini AI feedback |
| **Mock Interview** | Multi-round technical & behavioral simulations with adaptive follow-up challenges |
| **Performance Analytics** | Zero-DB local performance trends, topic breakdowns, and difficulty distributions |

---

## 🏗 Architecture (Database-Free / Stateless Backend)

```
┌────────────────────────────────────────────────────────┐
│               React 19 + TypeScript Frontend           │
│                                                        │
│  - Browser localStorage (Interviews, Mocks, Analytics) │
│  - 1-Click Candidate Demo Profile Mode                 │
│  - Split-pane Coding Workspace (Java/Python/TS)        │
│  - SPA Routing with Fallback (/index.html)             │
└───────────────────────────┬────────────────────────────┘
                            │ REST API (JSON / Bearer Token)
                            ▼
┌────────────────────────────────────────────────────────┐
│         Spring Boot 3.3.4 REST Backend (Port ${PORT})  │
│                                                        │
│  - Stateless AI Orchestration & Evaluation Engine      │
│  - Zero Database Dependency (No PostgreSQL/JPA needed) │
│  - Protected Gemini API Gateway (Header-Based Auth)    │
│  - Dynamic Port Binding & Container Health Check       │
└───────────────────────────┬────────────────────────────┘
                            │ HTTPS (Server-Side Only)
                            ▼
┌────────────────────────────────────────────────────────┐
│                   Google Gemini API                    │
│                  (gemini-3.8-flash)                    │
└────────────────────────────────────────────────────────┘
```

The system operates with a **stateless, zero-database architecture**:
- **Backend**: Java 21 + Spring Boot running statelessly on `${PORT:-8080}`. Performs AI orchestration, problem generation, and solution grading via Gemini. No database connection is required or attempted.
- **Frontend**: React 19 + Vite + TypeScript. Persists coding interview attempts, multi-round mock transcripts, and performance analytics locally in the candidate's browser storage.

---

## 🛠 Tech Stack

- **Frontend**: React 19, Vite, TypeScript, Tailwind CSS, React Router 7, Lucide React
- **Client Storage**: Typed, versioned browser `localStorage` service (`v1`) with corrupt-data self-healing and non-destructive clearing
- **Backend**: Java 21, Spring Boot 3.3.4, Spring Web, Spring Security, Bean Validation, JJWT
- **Persistence**: Database-Free (Zero PostgreSQL, MySQL, MongoDB, or external database requirement)
- **AI Integration**: Google Gemini API (`gemini-3.8-flash`), isolated exclusively to the Spring Boot server with deterministic heuristic fallbacks
- **Deployment Packaging**: Multi-stage `Dockerfile` (Java 21 JRE), `vercel.json`, `netlify.toml`, `public/_redirects`

---

## ✨ Features

1. **AI Problem Bank & Generator**:
   - Curated catalog of classic data structures, algorithms, and system design problems.
   - Dynamic Gemini problem generator for customized topics, roles, and difficulties.
2. **Interactive Coding Workspace**:
   - Split-pane coding interface with starter code across Java, Python, and TypeScript.
   - Algorithmic correctness evaluation, time complexity $O(n)$, and space complexity analysis.
   - Clear disclosure of AI evaluation without containerized remote sandbox execution.
3. **Multi-Round AI Mock Technical Interview**:
   - Supports `TECHNICAL`, `BEHAVIORAL`, and `MIXED` interview modes.
   - Multi-round questioning with dynamic follow-ups based on candidate answers.
   - Consolidated hiring committee final reports with readiness verdicts (`INTERVIEW READY`, `STRONG CANDIDATE`).
4. **Zero-DB Analytics Dashboard**:
   - Evaluates performance overview, topic strengths/weaknesses, and difficulty distributions directly from local browser history.
   - Zero fabricated demo data in user analytics.
5. **Local Candidate Demo Profile**:
   - Instant 1-click access (`candidate@codementor.ai`) without registration friction.
   - Clear disclosures regarding local client scoping.

---

## ⚙️ Environment Variables

### Backend Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `PORT` | Optional | `8080` | Port for Spring Boot HTTP server (provided automatically by Cloud Run, Render, Railway, Heroku) |
| `GEMINI_API_KEY` | Recommended | Empty (fallback heuristics used) | Google Gemini API key; isolated strictly on server-side |
| `GEMINI_MODEL` | Optional | `gemini-3.8-flash` | Gemini model identifier |
| `JWT_SECRET` | Optional | 512-bit built-in seed | Secret key for JWT signing (fails safely to secure seed if too short) |
| `CORS_ALLOWED_ORIGINS` | Optional | `http://localhost:5173,http://localhost:3000` | Comma-separated list of allowed frontend origins (e.g. `https://your-frontend.vercel.app`) |
| `APP_URL` | Optional | `http://localhost:5173` | Primary frontend application URL |

### Frontend Build-Time Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `VITE_API_BASE_URL` | Optional | `http://localhost:8080/api` (dev) or `/api` (prod) | Base URL of the Spring Boot REST backend (e.g. `https://your-backend.railway.app/api`) |

---

## 🔑 Google Gemini API Setup

1. Obtain a free or paid API key from [Google AI Studio](https://aistudio.google.com/).
2. Set `GEMINI_API_KEY` in the Spring Boot backend environment:
   ```bash
   export GEMINI_API_KEY="your-gemini-api-key"
   ```
3. The API key is sent via the secure `x-goog-api-key` header to Google Generative Language APIs and is **never** printed in server logs or sent to the browser.
4. When `GEMINI_API_KEY` is not set or quota is exhausted, the backend automatically transitions to deterministic algorithmic evaluation heuristics without throwing 500 errors.

---

## 💻 Local Development Instructions

### Prerequisites
- Node.js 18+ and npm
- Java 21 JDK
- Maven 3.8+

### 1. Start the Spring Boot Backend
```bash
cd backend
mvn spring-boot:run
```
The REST API will listen on `http://localhost:8080`.

Verify backend health:
```bash
curl http://localhost:8080/api/health
```

### 2. Start the React Frontend
In a separate terminal at repository root:
```bash
npm install
npm run dev
```
Open `http://localhost:3000` (or `http://localhost:5173`) in your browser.

---

## 🚢 Backend Deployment Instructions

The backend is fully self-contained with no database prerequisites.

### Option A: Cloud Run / Docker Container
A production multi-stage `backend/Dockerfile` is included:
```bash
# Build the Docker image from backend directory
cd backend
docker build -t codementor-backend .

# Run locally to test
docker run -p 8080:8080 \
  -e GEMINI_API_KEY="your_gemini_key" \
  -e CORS_ALLOWED_ORIGINS="https://your-frontend.vercel.app" \
  codementor-backend
```

Deploy to Google Cloud Run:
```bash
gcloud run deploy codementor-backend \
  --source ./backend \
  --platform managed \
  --region us-central1 \
  --set-env-vars "GEMINI_API_KEY=your_key,CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app" \
  --allow-unauthenticated
```

### Option B: Railway / Render / Heroku
1. Connect Git repository with root directory set to `backend`.
2. Build Command: `mvn clean package -DskipTests`
3. Start Command: `java -Dserver.port=$PORT -jar target/codementor-api-0.0.1-SNAPSHOT.jar`
4. Environment Variables:
   - `GEMINI_API_KEY`: Your Google AI Studio API key
   - `CORS_ALLOWED_ORIGINS`: Your deployed frontend origin (e.g. `https://codementor.vercel.app`)

---

## 🌐 Frontend Deployment Instructions

The React frontend compiles to static assets.

### Option A: Vercel
1. Import repository into Vercel.
2. Framework Preset: **Vite**
3. Build Command: `npm run build`
4. Output Directory: `dist`
5. Environment Variables:
   ```env
   VITE_API_BASE_URL=https://your-backend.railway.app/api
   ```
   *(The included `vercel.json` automatically manages client-side SPA routing rewrites).*

### Option B: Netlify
1. Import repository into Netlify.
2. Build Command: `npm run build`
3. Publish Directory: `dist`
4. Environment Variables:
   ```env
   VITE_API_BASE_URL=https://your-backend.railway.app/api
   ```
   *(The included `netlify.toml` and `public/_redirects` automatically route `/*` to `/index.html` with status 200).*

### Option C: Reverse-Proxy Setup (Single Domain / Nginx)
If proxying `/api` requests on the same domain:
```nginx
location /api/ {
    proxy_pass http://backend-service:8080/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
location / {
    root /var/www/dist;
    try_files $uri $uri/ /index.html;
}
```
In this mode, omit `VITE_API_BASE_URL`, and the frontend will automatically use relative `/api`.

---

## 🩺 Health Check & Monitoring

- **Application Health**: `GET /api/health`
  ```json
  {
    "status": "UP",
    "service": "CodeMentor AI",
    "database": "Stateless AI Engine (Zero Database)"
  }
  ```
- **Spring Actuator Health**: `GET /actuator/health`
  ```json
  {
    "status": "UP"
  }
  ```

---

## ⚠️ Known Limitations & Scope

1. **Local Browser Scoping**: Candidate interview sessions, code submissions, and analytics persist exclusively inside the candidate's active browser (`localStorage`). Sessions do not sync across different browsers or devices.
2. **Demo Authentication**: The demo candidate profile (`candidate@codementor.ai`) is engineered for rapid, friction-free evaluation. It does not provide multi-tenant cryptographic user isolation.
3. **AI Code Analysis vs. Container Sandbox**: Code submitted in the problem workspace and mock interviews is analyzed using Gemini AI for algorithmic complexity, correctness, and clean coding practices; it is not compiled or executed inside a multi-language containerized sandbox (e.g. Judge0).
4. **External Gemini Quota**: If the Gemini API key reaches upstream rate limits or quota boundaries (HTTP 429), the backend gracefully falls back to deterministic heuristic evaluation templates to ensure uninterrupted uptime.

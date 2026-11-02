# CodeMentor AI V1.6 — Database-Free Architecture Migration Plan

CodeMentor AI is transitioning to a **completely database-free, stateless architecture**. This migration eliminates all external database dependencies (PostgreSQL, Supabase, Neon, H2, SQLite) while preserving the full functionality of the platform: React 19 + TypeScript + Vite frontend, Java 21 + Spring Boot backend, real-time Gemini AI coding evaluations, multi-round technical mock interviews, and personalized analytics.

***

## User Review & Critical Decisions

> [!IMPORTANT]
> **No Database Requirement**: The application will run 100% out-of-the-box without requiring any database server, database connection strings (`DB_URL`), or database credentials. Spring Boot starts in milliseconds as a stateless AI computation and evaluation microservice.

- **Storage Strategy**: All user interview sessions, coding solutions, evaluation reports, mock interview rounds, and profile preferences are persisted securely inside the user's browser using versioned, typed `localStorage`.
- **Authentication Model**: Traditional database-backed multi-tenant user authentication is replaced with an intuitive **Local Demo Profile Mode**. Users can quickly enter as "Candidate Demo" or create custom local profiles with 1-click instant login, clearly indicating that data is scoped to the current browser session.
- **AI Service Isolation**: Google Gemini API calls (coding challenge generation, solution evaluation, mock interview interviewer simulation, follow-up questioning, and learning recommendations) remain strictly isolated on the Spring Boot backend (`/api/ai/*`, `/api/interviews/*`, `/api/mock-interviews/*`). The server never exposes API keys or secrets to the client.

***

## 1. Overview & Core Concept

- **What It Delivers**: A zero-setup, self-contained coding interview platform where engineers can practice algorithmic challenges and multi-round technical mock interviews with an AI interviewer, receiving instantaneous grading, code quality feedback, and analytics without configuring a database.
- **Target Audience**: Software engineering candidates preparing for technical rounds at top technology companies (SWE, Frontend, Backend, Full Stack).
- **Key Value**: Zero barrier to entry, instant startup, complete privacy (user code and solutions never touch an external database), and continuous reliability even in offline or sandboxed cloud environments.

***

## 2. User Experience & Visual Design

### Key User Flows
1. **Frictionless Entry**:
   - The landing page and login screen feature a **"Enter Demo Mode" / "1-Click Candidate Profile"** action.
   - A quiet notification badge informs the candidate: *"Running in Local Browser Mode · Zero Database Required"*.
2. **AI Coding Interview Workspace**:
   - Candidate chooses topic (e.g. Arrays, Trees, Dynamic Programming), difficulty (Easy, Medium, Hard), and language (Java, Python, TypeScript, Go, C++).
   - Spring Boot requests Gemini to generate tailored problems or loads standard LeetCode-style challenges.
   - Candidate writes solution in the code editor, runs tests, and clicks **Submit Solution**.
   - Gemini evaluates correctness, algorithmic time/space complexity, and code quality; results are saved directly to `localStorageService`.
3. **Multi-Round Technical Mock Interview**:
   - Candidate engages in an interactive conversational technical interview (Technical, Behavioral, or Mixed).
   - AI asks questions, candidate provides answers, and AI dynamically evaluates depth, communication, and issues follow-up challenges.
   - Final comprehensive assessment report is generated and archived locally.
4. **Performance Analytics & AI Insights**:
   - Dashboard automatically derives overview stats (Average Score, Completed Interviews, Highest/Lowest Score, Strongest/Weakest Topic) directly from locally stored interviews.
   - Performance trend chart renders historical scores chronologically.
   - Topic and language breakdown tables show proficiency distribution.
   - Candidate can trigger AI Insights to receive tailored action plans.
5. **Data Privacy & Reset Controls**:
   - In Settings, candidate can manage preferences (editor theme, keybindings) and use a dedicated **"Clear Local History"** action to wipe local storage cleanly.

### Visual Styling & Theme (Anti-Slop Discipline)
- **Palette**: Slate-950 dark terminal aesthetic with emerald accents (`#10b981`), indigo accents (`#6366f1`), and subtle border hierarchy (`border-slate-800`).
- **Zero-Pill Discipline**: Metadata (interview rounds, language, difficulty, dates) rendered as clean, unboxed text separated by typographic dots (`·`), never static candy pills.
- **Interactive Controls**: Functional segmented controls for language and difficulty selectors with active background fills and responsive hover transitions.

***

## 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Rationale & Trade-off |
| :--- | :--- | :--- |
| **Persistence Engine** | Browser `localStorage` with typed wrapper (`localStorageService.ts`) | **Zero Database Dependency**: Eliminates database outages, connection errors, and hosting setup costs. Trade-off: Data is specific to the current browser/device. |
| **Authentication Flow** | Local Demo Profile Mode with fast 1-click login and optional JWT fallback | Allows instantaneous testing and usage without password hashing errors or user table migrations. |
| **Backend Architecture** | Stateless Spring Boot 3.3.4 (Java 21) | Backend acts as an AI orchestration gateway and evaluation engine. Can be scaled horizontally or restarted with zero database state synchronization. |
| **AI Processing** | Spring Boot Gemini Service via REST | Gemini credentials remain protected on the server. Client never accesses or exposes `GEMINI_API_KEY`. |
| **Analytics Generation** | Client-side math calculation + Backend AI Insights | Immediate calculation of statistics without server round-trips; backend AI generates high-level strategic improvement plans. |

***

## 4. Technical Architecture & Component Hierarchy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        BROWSER FRONTEND (React 19)                     │
│                                                                        │
│   Landing / Login ──► Dashboard ──► Coding Interview ──► Mock Interview│
│            ▲                                    │              │       │
│            │ (Demo Profile Session)             ▼              ▼       │
│            │                       ┌─────────────────────────────────┐ │
│   ┌──────────────────┐             │     localStorageService.ts      │ │
│   │   AuthContext    │             │  - codementor_v1_interviews     │ │
│   │ (Local Demo Mode)│             │  - codementor_v1_mock_interviews│ │
│   └──────────────────┘             │  - codementor_v1_preferences    │ │
│                                    │  - Client Analytics Computation │ │
│                                    └─────────────────────────────────┘ │
└───────────────────────────────────────────────▲────────────────────────┘
                                                │ REST API (Bearer Token)
                                                ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     BACKEND (Spring Boot 3.3.4 / Java 21)              │
│                                                                        │
│   ┌────────────────────┐   ┌──────────────────┐  ┌──────────────────┐  │
│   │   AuthController   │   │ InterviewControl.│  │ MockInterviewCtrl│  │
│   │(Demo Token Issuer) │   │ (Coding Engine)  │  │(Rounds & Scoring)│  │
│   └─────────┬──────────┘   └────────┬─────────┘  └────────┬─────────┘  │
│             │                       │                     │            │
│             ▼                       ▼                     ▼            │
│   ┌──────────────────────────────────────────────────────────────────┐ │
│   │                         GeminiService.java                       │ │
│   │   - evaluateCodingSolution()                                     │ │
│   │   - evaluateMockAnswer() & generateFollowUp()                    │ │
│   │   - generateAiActionPlan() & personalizedInsights()              │ │
│   └─────────────────────────────────┬────────────────────────────────┘ │
└─────────────────────────────────────┼──────────────────────────────────┘
                                      │ HTTPS (Server-Side Only)
                                      ▼
                        ┌───────────────────────────┐
                        │     Google Gemini API     │
                        │    (gemini-3.8-flash)     │
                        └───────────────────────────┘
```

***

## 5. File Modification & Removal Inventory

### Files to Modify
1. **`src/services/api.ts`**:
   - Ensure full bi-directional persistence with `localStorageService`.
   - Update interview, mock interview, and analytics calls to seamlessly pull from local cache and sync evaluation results.
2. **`src/pages/LoginPage.tsx` & `src/pages/RegisterPage.tsx`**:
   - Add a prominent **"Continue in Demo Profile Mode"** button for 1-click access without requiring registration.
   - Add a banner explaining the database-free architecture.
3. **`src/context/AuthContext.tsx`**:
   - Support seamless instant login for demo candidate mode (`candidate@codementor.ai`).
4. **`src/components/Navbar.tsx`**:
   - Display a subtle **"Demo Profile Mode"** indicator in the navbar and mobile menu.
5. **`backend/src/main/java/com/codementor/api/exception/GlobalExceptionHandler.java`**:
   - Remove obsolete PostgreSQL / JDBC driver error string sanitization rules.
6. **`.env.example`**:
   - Remove all database environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
   - Retain only `GEMINI_API_KEY`, `JWT_SECRET`, and `VITE_API_BASE_URL`.
7. **`README.md`**:
   - Update system architecture documentation to explicitly describe the stateless, zero-database architecture.

### Files / Artifacts Verified Clean (No Changes Needed)
- **`backend/pom.xml`**: Already free of JPA/PostgreSQL starter dependencies; build and all 52 tests execute cleanly.
- **`backend/src/main/resources/application.properties`**: Already configured for stateless operation without datasource URLs.
- **`src/services/localStorageService.ts`**: Already initialized with robust error handling, schema versioning, and analytics calculations.
- **`backend/src/test/*`**: All 52 backend tests pass with 0 failures and 0 errors.

***

## 6. Verification & Validation Plan

- **Backend Validation**:
  - Run `mvn -f backend/pom.xml test` to verify all 52 unit and integration tests continue to pass.
  - Verify Spring Boot can start cleanly with `mvn spring-boot:run` or standalone JAR with no database running.
- **Frontend Validation**:
  - Run `npm run lint` (`tsc --noEmit`) to ensure zero TypeScript errors.
  - Run `npm run build` to verify production Vite bundle builds without errors.
  - Verify `localStorageService` accurately computes analytics with zero interviews and with populated interviews.

# Architecture — TaskFlow

**Project:** TaskFlow — Smart Task & Workflow Management Platform
**Document version:** 1.1
**Last updated:** 2026-06-23

This document describes the technical architecture of TaskFlow: how the system is decomposed, how a request flows from browser to database and back, how authentication works, and how the system is deployed.

---

## 1. High-Level Architecture

TaskFlow follows a classic **decoupled client–server** architecture: a single-page application (SPA) in the browser talks to a stateless REST API, which persists data in a document database.

```
┌──────────────┐        HTTPS / JSON         ┌─────────────────────┐
│              │  ───────────────────────▶   │                     │
│   Browser    │      REST + JWT (Bearer)    │   Spring Boot API    │
│  React SPA   │  ◀───────────────────────   │   (stateless)        │
│  (Vercel)    │                             │   (Railway)          │
└──────────────┘                             └──────────┬──────────┘
                                                        │ Mongo wire protocol
                                                        ▼
                                              ┌─────────────────────┐
                                              │   MongoDB Atlas      │
                                              │  (document store)    │
                                              └─────────────────────┘
```

**Key properties**

- **Stateless backend.** No HTTP session is stored server-side; identity travels in a signed JWT on each request. This makes the API horizontally scalable — any instance can serve any request.
- **Clear contract.** The frontend and backend share only the REST/JSON contract documented in `API_SPEC.md`. They deploy and scale independently.
- **Single source of truth.** All business rules (ownership, validation, status transitions) live in the backend service layer; the frontend never enforces security, only UX.

---

## 2. Frontend Architecture

**Stack:** React 18, Vite, TailwindCSS, React Router, Axios, React Query (TanStack Query) for server state.

### 2.1 Layering

```
src/
├── api/            # Axios instance + typed API call functions
├── auth/           # Auth context, token storage, route guards
├── components/     # Reusable presentational components (Button, Modal, Badge)
├── features/       # Feature modules (tasks, categories, dashboard, profile)
│   └── tasks/
│       ├── components/   # TaskCard, TaskForm, TaskFilters
│       ├── hooks/        # useTasks, useCreateTask (React Query)
│       └── pages/        # TasksPage
├── hooks/          # Cross-cutting hooks
├── layouts/        # AppLayout, AuthLayout
├── lib/            # Helpers (date formatting, constants, enums)
├── pages/          # Top-level routed pages
├── routes/         # Route definitions + ProtectedRoute
└── main.jsx
```

### 2.2 Responsibilities

- **API layer (`src/api`).** A single configured Axios instance reads the base URL from `VITE_API_URL`, attaches the `Authorization: Bearer <token>` header via a request interceptor, and handles `401` responses by attempting a silent token refresh (response interceptor) before redirecting to login.
- **Server state.** React Query owns all server-derived data — caching, background refetching, and optimistic updates for task mutations. Components do not store fetched lists in local state.
- **Client state.** Lightweight UI state (modals, filters, form inputs) lives in component state or small contexts. Auth state (current user, tokens) lives in an `AuthContext`.
- **Routing & guards.** `ProtectedRoute` redirects unauthenticated users to `/login`. Public routes (landing, login, register) use a separate layout.
- **Styling.** TailwindCSS utility classes with a small set of shared component primitives; design tokens (colours, spacing) centralised in `tailwind.config.js`.

### 2.3 Data flow (frontend)

UI event → React Query hook → Axios API function → backend → response cached by React Query → components re-render from cache. Mutations invalidate the relevant query keys (e.g. creating a task invalidates `['tasks']` and `['dashboard','stats']`).

---

## 3. Backend Architecture

**Stack:** Java 21, Spring Boot 4.1, Spring Web MVC, Spring Security, Spring Data MongoDB, JJWT 0.12.x (HS256 tokens), Bean Validation (Jakarta Validation), Lombok.

### 3.1 Layered design

```
Controller  →  Service  →  Repository  →  MongoDB
   ▲             ▲             ▲
  DTOs        Domain        Documents
            business logic
```

- **Controller layer.** Thin. Maps HTTP to method calls, validates request DTOs (`@Valid`), and returns response DTOs with appropriate status codes. No business logic.
- **Service layer.** Owns all business rules: ownership checks, status-transition logic, `completedAt` handling, overdue computation, dashboard aggregation. This is where authorisation (does *this* user own *this* task?) is enforced.
- **Repository layer.** Spring Data MongoDB interfaces (`MongoRepository`) plus custom queries for filtering, sorting, and aggregation.
- **Domain / documents.** `@Document` POJOs mapped to MongoDB collections.
- **DTOs.** Separate request and response objects so the wire contract is decoupled from the persistence model (never expose `passwordHash`, never accept `userId` from the client).

### 3.2 Package structure

```
com.taskflow.backend
├── config/            # SecurityConfig, MongoConfig, PasswordConfig  (CorsConfig/OpenApiConfig planned)
├── security/          # JwtService, JwtAuthenticationFilter, JwtAuthenticationEntryPoint,
│                      #   JwtProperties, SecurityUtils
├── auth/              # AuthController, AuthService, dto/ (Register/Login/Refresh requests, AuthResponse)
├── user/              # UserController, UserService, UserRepository, UserMapper, User document, dto/
├── task/              # TaskController, TaskService, TaskRepository, Task document, dto/
├── category/          # CategoryController, CategoryService, CategoryRepository, Category, dto/
├── dashboard/         # DashboardController, DashboardService (aggregations), dto/
├── common/            # Enums (TaskStatus, Priority, Role), exceptions, ApiError, GlobalExceptionHandler
└── BackendApplication.java
```

> Authentication uses the JWT `SecurityContext` directly (the filter builds the
> `Authentication`); there is no Spring `UserDetailsService` — its auto-config is
> excluded in `BackendApplication`. `SecurityUtils.currentUserId()` exposes the
> authenticated `sub` to the service layer.

### 3.3 Cross-cutting concerns

- **Validation.** Jakarta Bean Validation annotations on request DTOs; enum membership and length constraints enforced before reaching the service.
- **Error handling.** A `@RestControllerAdvice` global handler (`GlobalExceptionHandler`) converts exceptions into a consistent `ApiError` JSON body (see `API_SPEC.md`) — duplicate email → `409`, invalid credentials / invalid token → `401`, validation & malformed JSON → `400`, with a logged catch-all `500`. Unauthenticated access to protected routes is rendered as the same `ApiError` shape by `JwtAuthenticationEntryPoint`.
- **Security.** `SecurityConfig` defines a stateless (`SessionCreationPolicy.STATELESS`) filter chain with CSRF, HTTP Basic, and form login disabled; permits `/api/v1/auth/**`, requires authentication for all other API routes; and registers `JwtAuthenticationFilter` ahead of the username/password filter. (CORS restriction to the Vercel origin is a planned `CorsConfig`.)
- **Mapping.** DTO ↔ domain mapping via explicit mappers (or MapStruct) — no leaking of internal fields.

---

## 4. Authentication Flow (JWT)

TaskFlow uses **stateless JWT authentication** with short-lived access tokens and longer-lived refresh tokens.

### 4.1 Registration & login

```
1. Client POST /api/v1/auth/register  { name, email, password }
2. Server validates, hashes password (BCrypt), stores user, and immediately
   issues a token pair (auto-login) — returns 201 with both tokens + profile.

3. Client POST /api/v1/auth/login  { email, password }
4. Server verifies credentials, then issues:
      - accessToken  (JWT HS256, ~15 min, claims: sub=userId, email, role, typ=access, iat, exp)
      - refreshToken (JWT HS256, ~7 days, typ=refresh)
5. Server returns both tokens + safe user profile (tokenType=Bearer, expiresIn=900).
```

### 4.2 Authenticated request

```
Browser                         Spring Boot
   │  GET /api/v1/tasks                │
   │  Authorization: Bearer <access>   │
   │ ─────────────────────────────────▶│
   │                                   │ JwtAuthenticationFilter:
   │                                   │   1. extract Bearer token
   │                                   │   2. validate signature + expiry, require typ=access
   │                                   │   3. build Authentication (principal=userId, ROLE_<role>)
   │                                   │   4. set SecurityContext
   │                                   │ Controller → Service (scoped by userId)
   │  200 OK  [ tasks ]                 │
   │ ◀─────────────────────────────────│
```

If the access token is missing, malformed, expired, or not an access token, the filter leaves the context unauthenticated and the chain returns `401 Unauthorized` (as an `ApiError` body) via `JwtAuthenticationEntryPoint`.

### 4.3 Token refresh

```
1. Access token expires → API returns 401.
2. Client (Axios response interceptor) POST /api/v1/auth/refresh { refreshToken }.
3. Server validates the refresh token and issues a new access token.
4. Client retries the original request transparently.
5. If the refresh token is also invalid/expired → client clears tokens, redirects to /login.
```

### 4.4 Token storage & hardening

- Access token kept in memory (or short-lived storage); refresh token stored so a returning user stays signed in.
- Tokens are signed **HS256** with a strong secret (`JWT_SECRET`, ≥ 256 bits) and contain no sensitive data beyond `userId`, `email`, and `role`.
- Logout is currently a **stateless client-side discard** (server returns `204`, tokens remain valid until expiry). A server-side refresh-token denylist to hard-revoke sessions is a planned enhancement.

---

## 5. Request Flow (end-to-end)

A worked example: **user creates a task.**

```
1. User submits the "New Task" form in the React SPA.
2. useCreateTask() (React Query mutation) calls api.createTask(payload).
3. Axios attaches Authorization: Bearer <accessToken>, POSTs to
   https://<api>/api/v1/tasks with the JSON body.
4. CORS preflight passes (Vercel origin allowed).
5. JwtAuthenticationFilter validates the access token, populates SecurityContext with userId.
6. TaskController validates the @Valid CreateTaskRequest DTO.
7. TaskService:
      - sets userId from the authenticated principal (never the client),
      - validates categoryId belongs to the same user (if provided),
      - applies defaults (status=TODO), persists via TaskRepository.
8. MongoDB inserts the document, returns the generated _id.
9. Service maps the saved document → TaskResponse DTO.
10. Controller returns 201 Created with the task body.
11. React Query caches the result, invalidates ['tasks'] and ['dashboard','stats'];
    the UI updates optimistically/refetches.
```

---

## 6. Folder Structure Recommendations

Top-level monorepo layout:

```
TaskFlow/
├── docs/
│   ├── SRS.md
│   ├── ARCHITECTURE.md
│   ├── DATABASE.md
│   └── API_SPEC.md
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── api/
│   │   ├── auth/
│   │   ├── components/
│   │   ├── features/
│   │   ├── hooks/
│   │   ├── layouts/
│   │   ├── lib/
│   │   ├── pages/
│   │   ├── routes/
│   │   └── main.jsx
│   ├── .env.example
│   ├── index.html
│   ├── tailwind.config.js
│   ├── vite.config.js
│   └── package.json
├── backend/
│   ├── src/main/java/com/taskflow/
│   │   ├── config/
│   │   ├── security/
│   │   ├── auth/
│   │   ├── user/
│   │   ├── task/
│   │   ├── category/
│   │   ├── dashboard/
│   │   └── common/
│   ├── src/main/resources/
│   │   └── application.properties   # (application-prod.properties for prod overrides, planned)
│   ├── src/test/java/com/taskflow/
│   ├── .env.example
│   ├── pom.xml
│   └── Dockerfile
└── README.md
```

**Conventions**

- One package per bounded concern (`task`, `category`, `user`) — keeps controllers, services, repositories, and DTOs for a feature together.
- Configuration lives in `config/`; security in `security/`; shared enums and exceptions in `common/`.
- `.env.example` files document required variables without committing secrets.

---

## 7. Deployment Architecture

```
        Developer
           │ git push
           ▼
      ┌──────────┐
      │  GitHub  │  (source of truth, CI on PR)
      └────┬─────┘
           │ webhooks
   ┌───────┴────────┐
   ▼                ▼
┌────────┐     ┌──────────┐         ┌────────────────┐
│ Vercel │     │ Railway  │ ──────▶ │ MongoDB Atlas  │
│ React  │     │ Spring   │  TLS    │ (managed DB)   │
│ SPA    │     │ Boot API │         └────────────────┘
└───┬────┘     └────┬─────┘
    │ HTTPS         │
    └──────────────▶│  (browser → API)
```

| Concern | Choice | Notes |
|---------|--------|-------|
| **Frontend hosting** | Vercel | Builds from `frontend/`; serves static SPA over global CDN; auto-deploys on push. |
| **Backend hosting** | Railway | Builds the Spring Boot jar (or Docker image) from `backend/`; exposes an HTTPS endpoint; auto-deploys on push. |
| **Database** | MongoDB Atlas | Managed cluster; IP allow-list / network rules restrict access to Railway. |
| **DNS / domains** | Provider of choice | Custom domain → Vercel for the app; API on a Railway subdomain. |
| **CI** | GitHub Actions | Run tests and build on pull requests before merge. |

**Environment configuration**

- Backend reads `MONGODB_URI`, `JWT_SECRET`, `JWT_ACCESS_TOKEN_EXPIRATION`, `JWT_REFRESH_TOKEN_EXPIRATION`, `CORS_ALLOWED_ORIGINS` from Railway environment variables (Spring relaxed binding maps these to the `jwt.*` properties). Expirations are durations (e.g. `15m`, `7d`), not millisecond integers.
- Frontend reads `VITE_API_URL` (the Railway API base URL) at build time from Vercel environment variables.
- CORS on the backend is restricted to the deployed Vercel origin(s).

**Scaling & resilience**

- The stateless API scales horizontally — add instances behind Railway's load balancing without sticky sessions.
- MongoDB Atlas handles replication and backups.
- A `/actuator/health` endpoint provides liveness/readiness signals for platform health checks.

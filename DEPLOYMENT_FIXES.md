# TaskFlow — Deployment Fixes

Implementation report for the three confirmed deployment issues (B1, B3, B4) plus
repository cleanup. The false positive **B2** (`spring.mongodb.uri` →
`spring.data.mongodb.uri`) was **not** changed — the existing key is correct for
Spring Boot 4.x and was verified working against MongoDB Atlas during testing.

**Verification status:** Backend builds (`mvnw package`), starts against MongoDB
Atlas, serves the health check, enforces the CORS allow-list, and completes a full
auth round-trip. Frontend builds (`vite build`). Details in §5–6.

---

## 1. Changes Made

### Files modified
| File | Change |
|------|--------|
| `backend/src/main/resources/application.properties` | Mongo URI now `${MONGODB_URI:...}` (localhost fallback); added `server.port=${PORT:8080}` |
| `backend/src/main/java/com/taskflow/backend/config/SecurityConfig.java` | Enabled `http.cors(...)`, permitted public `GET /health` |
| `backend/src/main/java/com/taskflow/backend/config/package-info.java` | Doc: `SecurityConfig`/`CorsConfig` now exist (removed from TODO) |
| `backend/.gitignore` | Ignore `.env`, `.env.local`, `*.log` |
| `frontend/.env.example` | Updated stale Railway reference → Render |

### Files added
| File | Purpose |
|------|---------|
| `backend/src/main/java/com/taskflow/backend/config/CorsConfig.java` | Env-driven `CorsConfigurationSource` bean |
| `backend/src/main/java/com/taskflow/backend/health/HealthController.java` | Lightweight public `GET /health` |
| `backend/.env.example` | Backend env var template (no real secrets) |
| `backend/Dockerfile` | Multi-stage Java 21 build for Render |
| `backend/.dockerignore` | Keeps build context lean / excludes secrets & logs |
| `render.yaml` | Render Blueprint (Docker web service + health check) |
| `.gitignore` (repo root) | Ignore `IMP_Details.txt`, `.env`, logs |

### Files untracked (removed from git, kept on disk)
| File | Action |
|------|--------|
| `backend/app.log` | `git rm --cached` (committed runtime log) — now gitignored |

---

## 2. Security Improvements

**Secrets removed from source control:**

- **MongoDB Atlas connection string** (with username `shaikhzain242_db_user` and
  password) was hardcoded at `application.properties:21`. It is now read from the
  `MONGODB_URI` environment variable:
  ```properties
  spring.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/taskflow}
  ```
  The property **key was intentionally left as `spring.mongodb.uri`** (B2 false
  positive — confirmed correct for Spring Boot 4.x and verified against Atlas).

- **`IMP_Details.txt`** (plaintext Atlas username/password/connection string) is
  now gitignored so it can never be committed. It was *untracked* already, so no
  history rewrite was needed for it.

- **`JWT_SECRET`** was already env-overridable (`${JWT_SECRET:dev-only...}`) and is
  documented in `.env.example`; the committed value is a clearly-labelled dev-only
  placeholder, not a production secret.

- Logs (`backend/app.log`) and `.env` files are now gitignored.

> ⚠️ See **§6 Remaining Risks** — the Atlas credentials were present in earlier
> commits, so they must be **rotated**.

**Local development preserved:** with no env vars set, the app falls back to a
local mongod (`mongodb://localhost:27017/taskflow`) and a dev JWT secret. To use
Atlas locally, set `MONGODB_URI` (see `backend/.env.example`).

---

## 3. CORS Implementation

CORS was previously only *documented* (comments in `SecurityConfig` and a TODO in
`package-info`) but never wired up; dev relied on the Vite proxy. It is now a real
Spring Security–integrated policy.

- **`CorsConfig`** exposes a `CorsConfigurationSource` bean:
  - **Origins:** read from `CORS_ALLOWED_ORIGINS` (comma-separated), defaulting to
    `http://localhost:5173` for local dev. Set it to the Vercel URL in production.
  - **Methods:** `GET, POST, PUT, PATCH, DELETE, OPTIONS`.
  - **Headers:** `Authorization, Content-Type`.
  - **Credentials:** disabled (auth is stateless Bearer tokens in the
    `Authorization` header, not cookies), which keeps explicit origins valid.
  - **Max-Age:** 3600s (caches preflight).
- **`SecurityConfig`** consumes the bean via `http.cors(c -> c.configurationSource(...))`,
  so the policy is enforced inside the filter chain. Preflight `OPTIONS /**` and the
  `/health` probe remain permitted.

**Verified behaviour (live server):**
| Request | Result |
|---------|--------|
| `OPTIONS` preflight from `https://taskflow.vercel.app` | `200` + `Access-Control-Allow-Origin/Methods/Headers` |
| `OPTIONS` preflight from `https://evil.example.com` | `403` (rejected) |

---

## 4. Render Deployment Configuration

| File | Why |
|------|-----|
| `backend/Dockerfile` | Pins **Java 21** (`eclipse-temurin:21`), reproducible multi-stage build (deps cached, then `mvnw package`), runs the jar as a non-root user. Removes reliance on Render auto-detecting the build, which is the most common cause of failed Spring Boot deploys. |
| `backend/.dockerignore` | Excludes `target/`, logs, `.env`, `.git` from the build context — faster builds, no secret leakage into the image. |
| `render.yaml` | Render Blueprint: Docker web service, `healthCheckPath: /health`, and **declared-but-not-valued** secret env vars (`sync: false`) so they are set in the dashboard, never in git. |

**Port binding:** `server.port=${PORT:8080}` — binds to Render's injected `$PORT`,
falls back to `8080` locally. Verified: the app bound to a custom `PORT=8089`
during testing.

**Health check:** `GET /health` → `200 {"status":"UP"}`. Public (permitted in
`SecurityConfig`), does no I/O so it can't be tripped by transient DB latency.
A custom controller was chosen over `spring-boot-starter-actuator` to keep it
dependency-free and lightweight, as requested.

---

## 5. Manual Deployment Steps

### MongoDB Atlas
1. **Rotate the exposed credentials** (see §6): Atlas → *Database Access* → edit the
   user → *Edit Password* / create a fresh user, drop the old one.
2. *Network Access* → add `0.0.0.0/0` (or Render's egress IPs) so Render can connect.
3. Copy the new SRV connection string (include `/taskflow` db name) for the next step.

### Render (backend)
1. New → **Blueprint**, point at the repo (uses `render.yaml`), **or** create a
   *Web Service* with **Runtime = Docker**, root/context `./backend`.
2. Set environment variables (Dashboard → *Environment*):
   - `MONGODB_URI` = new Atlas SRV string
   - `JWT_SECRET` = a 32+ char random secret
   - `CORS_ALLOWED_ORIGINS` = your Vercel URL, e.g. `https://taskflow.vercel.app`
   - *(do not set `PORT` — Render injects it)*
3. Health check path is `/health`. Deploy and confirm it goes live.

### Vercel (frontend)
1. Import the repo, set **Root Directory = `frontend`** (framework: Vite).
2. Set env var `VITE_API_URL` = `https://<your-service>.onrender.com/api/v1`.
3. Deploy, then copy the resulting Vercel URL back into the backend's
   `CORS_ALLOWED_ORIGINS` (multiple origins allowed, comma-separated) and redeploy
   the backend if it changed.

---

## 6. Remaining Risks

1. **Exposed credentials must be rotated.** The Atlas password and the dev
   `JWT_SECRET` placeholder appear in prior git history (e.g. the Phase 5B commit).
   Removing them from the working tree does not purge history — **rotate the Atlas
   user/password** and generate a fresh production `JWT_SECRET`. Optionally scrub
   history (`git filter-repo` / BFG) and force-push if the remote is private to you.
2. **`IMP_Details.txt` still exists locally** with plaintext secrets. It's now
   gitignored (won't be committed) but was left on disk intentionally — delete it
   after you've rotated and stored the new credentials in a password manager.
3. **`backend/app.log` left on disk** (untracked/ignored). Harmless; delete if you
   want a clean tree.
4. **Atlas free-tier `0.0.0.0/0` allow-list** is broad. Prefer restricting to
   Render's static egress IPs once known.
5. **Render free plan cold starts** — the service sleeps after inactivity; the first
   request (and health check) after idle can take ~30–60s. Use a paid plan or an
   external pinger if you need always-on.
6. **No automated tests were run for the deploy changes** beyond build + manual
   smoke tests (startup, health, CORS, register/login/authenticated GET). The
   existing test suite was not executed as part of this task.

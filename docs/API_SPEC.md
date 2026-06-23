# API Specification — TaskFlow

**Project:** TaskFlow — Smart Task & Workflow Management Platform
**Document version:** 1.1
**Base URL (prod):** `https://<railway-app>.up.railway.app`
**API base path:** `/api/v1`
**Content type:** `application/json`
**Last updated:** 2026-06-23

> **Implementation status.** §1 (conventions), §2 (Authentication), §3.1–§3.3
> (profile read/update, change password), §4 Tasks CRUD (4.1–4.4, 4.6), and §5
> Categories (full CRUD) are **implemented** — all behind stateless JWT auth
> (HS256), user-scoped, with filtering/pagination/sorting on task listing. Not yet
> implemented: §4.5 (`PATCH /tasks/{id}/status` — status can be changed via the
> §4.4 `PUT` for now), §3.4 (delete account), §6 (Dashboard), and §7 (Admin).

---

## 1. Conventions

### 1.1 Authentication

Protected endpoints require a bearer token:

```
Authorization: Bearer <accessToken>
```

The token is a JWT (signed **HS256**) issued by `/auth/register`, `/auth/login`, or `/auth/refresh`. Claims: `sub` = user id, `email`, `role`, `typ` (`access` or `refresh`), plus `iat`/`exp`. All data access is scoped to the `sub` user server-side.

Two token kinds are issued:

| Token | Lifetime (default) | Used for |
|-------|--------------------|----------|
| `accessToken` | 15 min (`expiresIn` = 900 s) | Sent on every request as `Authorization: Bearer`. |
| `refreshToken` | 7 days | Exchanged at `/auth/refresh` for a new access token. Never sent as a Bearer credential. |

Only an **access** token authenticates a request; presenting a refresh token as a Bearer credential is rejected. A missing, malformed, expired, or wrong-kind token on a protected endpoint returns `401` with the standard error body (§1.3). Token lifetimes are configurable (`jwt.access-token-expiration`, `jwt.refresh-token-expiration`).

### 1.2 Standard status codes

| Code | Meaning |
|------|---------|
| `200 OK` | Successful read/update. |
| `201 Created` | Resource created. |
| `204 No Content` | Successful delete (no body). |
| `400 Bad Request` | Validation error / malformed input. |
| `401 Unauthorized` | Missing/invalid/expired token. |
| `403 Forbidden` | Authenticated but not allowed (e.g. not the owner). |
| `404 Not Found` | Resource does not exist (or not owned by caller). |
| `409 Conflict` | Uniqueness violation (e.g. email/category name already exists). |
| `422 Unprocessable Entity` | Semantically invalid (e.g. invalid status transition). |
| `500 Internal Server Error` | Unexpected server error. |

### 1.3 Error response shape

All errors return a consistent body:

```json
{
  "timestamp": "2026-06-22T10:15:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/tasks",
  "details": [
    { "field": "title", "issue": "must not be blank" }
  ]
}
```

### 1.4 Pagination, filtering, sorting

List endpoints accept query parameters:

| Param | Example | Description |
|-------|---------|-------------|
| `page` | `0` | Zero-based page index (default `0`). |
| `size` | `20` | Page size (default `20`, max `100`). |
| `sort` | `dueDate,asc` | `field,direction`. |

Paged responses are wrapped:

```json
{
  "content": [ /* items */ ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3,
  "hasNext": true
}
```

---

## 2. Authentication

### 2.1 Register

| | |
|---|---|
| **Method** | `POST` |
| **Route** | `/api/v1/auth/register` |
| **Auth** | None |

**Request body**
```json
{
  "name": "Ananya Sharma",
  "email": "ananya@example.com",
  "password": "S3cure!pass"
}
```

**Response body — `201 Created`** — registration auto-logs-in, returning the same token pair + profile as login (§2.2):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "665f1a2b3c4d5e6f7a8b9c01",
    "name": "Ananya Sharma",
    "email": "ananya@example.com",
    "role": "USER",
    "avatarUrl": null,
    "timezone": "Asia/Kolkata",
    "createdAt": "2026-06-01T08:30:00Z",
    "updatedAt": "2026-06-01T08:30:00Z"
  }
}
```
> `password` is required, 8–72 chars; `name` 2–60 chars. The email is stored lowercased/trimmed.

**Status codes:** `201` created · `400` validation error · `409` email already registered.

---

### 2.2 Login

| | |
|---|---|
| **Method** | `POST` |
| **Route** | `/api/v1/auth/login` |
| **Auth** | None |

**Request body**
```json
{
  "email": "ananya@example.com",
  "password": "S3cure!pass"
}
```

**Response body — `200 OK`**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "665f1a2b3c4d5e6f7a8b9c01",
    "name": "Ananya Sharma",
    "email": "ananya@example.com",
    "role": "USER",
    "avatarUrl": null,
    "timezone": "Asia/Kolkata",
    "createdAt": "2026-06-01T08:30:00Z",
    "updatedAt": "2026-06-01T08:30:00Z"
  }
}
```
> `tokenType` is always `"Bearer"`; `expiresIn` is the access-token lifetime in seconds. `user` is the full profile object (same shape as §3.1).

**Status codes:** `200` success · `400` validation error · `401` invalid credentials.

---

### 2.3 Refresh token

| | |
|---|---|
| **Method** | `POST` |
| **Route** | `/api/v1/auth/refresh` |
| **Auth** | None (refresh token in body) |

**Request body**
```json
{ "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6..." }
```

**Response body — `200 OK`**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```
> Returns a fresh **access** token only; the refresh token is unchanged and is reused until it expires. `refreshToken` and `user` are omitted from this response (null fields are not serialised). The supplied token must be a valid, non-expired **refresh**-type token — an access token here is rejected with `401`.

**Status codes:** `200` success · `400` `refreshToken` missing/blank · `401` invalid/expired/wrong-kind refresh token.

---

### 2.4 Logout

| | |
|---|---|
| **Method** | `POST` |
| **Route** | `/api/v1/auth/logout` |
| **Auth** | None (public route) |

**Request body**
```json
{ "refreshToken": "eyJhbGciOiJIUzI1NiJ9..." }
```

**Response — `204 No Content`** (no body).

> **Current behaviour:** logout is a **stateless client-side discard** — the server accepts the request and returns `204`; the client clears its stored tokens. Because tokens are stateless, the access token remains valid until it expires. A server-side refresh-token **denylist** (to hard-revoke a session immediately) is a planned enhancement. The route is public (under `/auth/**`); `refreshToken` is still required in the body for forward compatibility.

**Status codes:** `204` success · `400` `refreshToken` missing/blank.

---

## 3. User Profile

### 3.1 Get current user

| | |
|---|---|
| **Method** | `GET` |
| **Route** | `/api/v1/users/me` |
| **Auth** | Required |

**Response body — `200 OK`**
```json
{
  "id": "665f1a2b3c4d5e6f7a8b9c01",
  "name": "Ananya Sharma",
  "email": "ananya@example.com",
  "role": "USER",
  "avatarUrl": null,
  "timezone": "Asia/Kolkata",
  "createdAt": "2026-06-01T08:30:00Z",
  "updatedAt": "2026-06-01T08:30:00Z"
}
```

**Status codes:** `200` success · `401` not authenticated.

---

### 3.2 Update profile

| | |
|---|---|
| **Method** | `PUT` |
| **Route** | `/api/v1/users/me` |
| **Auth** | Required |

**Request body** (fields optional; `email`/`role` immutable here)
```json
{
  "name": "Ananya S.",
  "avatarUrl": "https://cdn.example.com/a.png",
  "timezone": "Asia/Kolkata"
}
```

**Response body — `200 OK`** — the updated user object (same shape as 3.1).

**Status codes:** `200` success · `400` validation error · `401` not authenticated.

---

### 3.3 Change password

| | |
|---|---|
| **Method** | `PATCH` |
| **Route** | `/api/v1/users/me/password` |
| **Auth** | Required |

**Request body**
```json
{
  "currentPassword": "S3cure!pass",
  "newPassword": "Ev3nM0re!secure"
}
```

**Response — `204 No Content`**.

**Status codes:** `204` success · `400` weak/invalid new password · `401` not authenticated · `403` current password incorrect.

---

### 3.4 Delete account

| | |
|---|---|
| **Method** | `DELETE` |
| **Route** | `/api/v1/users/me` |
| **Auth** | Required |

**Response — `204 No Content`**. Cascades: the user's tasks and categories are deleted.

**Status codes:** `204` success · `401` not authenticated.

---

## 4. Tasks

The `Task` response object:

```json
{
  "id": "665f1a2b3c4d5e6f7a8b9c20",
  "title": "Finish DBMS assignment",
  "description": "Normalize the schema to 3NF.",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "dueDate": "2026-06-25T18:30:00Z",
  "completedAt": null,
  "categoryId": "665f1a2b3c4d5e6f7a8b9c10",
  "tags": ["academic", "deadline"],
  "isOverdue": false,
  "createdAt": "2026-06-02T10:15:00Z",
  "updatedAt": "2026-06-03T07:45:00Z"
}
```

### 4.1 List tasks

| | |
|---|---|
| **Method** | `GET` |
| **Route** | `/api/v1/tasks` |
| **Auth** | Required |

**Query parameters**

| Param | Example | Description |
|-------|---------|-------------|
| `status` | `TODO` | Filter by status. |
| `priority` | `HIGH` | Filter by priority. |
| `categoryId` | `665f...c10` | Filter by category. |
| `search` | `assignment` | Case-insensitive keyword search (title + description). |
| `overdue` | `true` | Only overdue tasks (`dueDate < now && status != DONE`). |
| `dueAfter` | `2026-06-01T00:00:00Z` | Only tasks with `dueDate >=` this instant. |
| `dueBefore` | `2026-06-30T00:00:00Z` | Only tasks with `dueDate <=` this instant. |
| `page`, `size`, `sort` | `0`, `20`, `dueDate,asc` | Pagination/sorting (default size 20, max 100). |

**Response body — `200 OK`** — a paged wrapper of `Task` objects (see §1.4).

**Status codes:** `200` success · `400` invalid filter value · `401` not authenticated.

---

### 4.2 Create task

| | |
|---|---|
| **Method** | `POST` |
| **Route** | `/api/v1/tasks` |
| **Auth** | Required |

**Request body**
```json
{
  "title": "Finish DBMS assignment",
  "description": "Normalize the schema to 3NF.",
  "priority": "HIGH",
  "dueDate": "2026-06-25T18:30:00Z",
  "categoryId": "665f1a2b3c4d5e6f7a8b9c10",
  "tags": ["academic", "deadline"]
}
```
> `status` defaults to `TODO`; `priority` defaults to `MEDIUM` if omitted. `userId` is taken from the token, never the body.

**Response body — `201 Created`** — the created `Task` object.

**Status codes:** `201` created · `400` validation error · `401` not authenticated · `404` `categoryId` not found / not owned by user.

---

### 4.3 Get task by id

| | |
|---|---|
| **Method** | `GET` |
| **Route** | `/api/v1/tasks/{id}` |
| **Auth** | Required |

**Response body — `200 OK`** — the `Task` object.

**Status codes:** `200` success · `401` not authenticated · `404` not found / not owned.

---

### 4.4 Update task

| | |
|---|---|
| **Method** | `PUT` |
| **Route** | `/api/v1/tasks/{id}` |
| **Auth** | Required |

**Request body** (full update of editable fields)
```json
{
  "title": "Finish DBMS assignment (final)",
  "description": "Normalize to 3NF + ER diagram.",
  "priority": "HIGH",
  "dueDate": "2026-06-26T18:30:00Z",
  "categoryId": "665f1a2b3c4d5e6f7a8b9c10",
  "tags": ["academic"]
}
```

**Response body — `200 OK`** — the updated `Task` object.

**Status codes:** `200` success · `400` validation error · `401` not authenticated · `404` not found / not owned.

---

### 4.5 Update task status (workflow transition)

| | |
|---|---|
| **Method** | `PATCH` |
| **Route** | `/api/v1/tasks/{id}/status` |
| **Auth** | Required |

**Request body**
```json
{ "status": "DONE" }
```
> On transition to `DONE`, the server sets `completedAt`. On transition away from `DONE`, it clears `completedAt`.

**Response body — `200 OK`** — the updated `Task` object.

**Status codes:** `200` success · `401` not authenticated · `404` not found / not owned · `422` invalid status value.

---

### 4.6 Delete task

| | |
|---|---|
| **Method** | `DELETE` |
| **Route** | `/api/v1/tasks/{id}` |
| **Auth** | Required |

**Response — `204 No Content`**.

**Status codes:** `204` success · `401` not authenticated · `404` not found / not owned.

---

## 5. Categories

The `Category` response object:

```json
{
  "id": "665f1a2b3c4d5e6f7a8b9c10",
  "name": "University",
  "color": "#54C8EE",
  "taskCount": 7,
  "createdAt": "2026-06-01T09:00:00Z",
  "updatedAt": "2026-06-01T09:00:00Z"
}
```
> `taskCount` is computed (number of the user's tasks in this category).

### 5.1 List categories

| | |
|---|---|
| **Method** | `GET` |
| **Route** | `/api/v1/categories` |
| **Auth** | Required |

**Response body — `200 OK`**
```json
[
  { "id": "665f...c10", "name": "University", "color": "#54C8EE", "taskCount": 7, "createdAt": "...", "updatedAt": "..." },
  { "id": "665f...c11", "name": "Freelance",  "color": "#7E2037", "taskCount": 3, "createdAt": "...", "updatedAt": "..." }
]
```

**Status codes:** `200` success · `401` not authenticated.

---

### 5.2 Create category

| | |
|---|---|
| **Method** | `POST` |
| **Route** | `/api/v1/categories` |
| **Auth** | Required |

**Request body**
```json
{ "name": "University", "color": "#54C8EE" }
```

**Response body — `201 Created`** — the created `Category` object.

**Status codes:** `201` created · `400` validation error (bad colour/name) · `401` not authenticated · `409` category name already exists for this user.

---

### 5.3 Get category by id

| | |
|---|---|
| **Method** | `GET` |
| **Route** | `/api/v1/categories/{id}` |
| **Auth** | Required |

**Response body — `200 OK`** — the `Category` object.

**Status codes:** `200` success · `401` not authenticated · `404` not found / not owned.

---

### 5.4 Update category

| | |
|---|---|
| **Method** | `PUT` |
| **Route** | `/api/v1/categories/{id}` |
| **Auth** | Required |

**Request body**
```json
{ "name": "Coursework", "color": "#0E4A63" }
```

**Response body — `200 OK`** — the updated `Category` object.

**Status codes:** `200` success · `400` validation error · `401` not authenticated · `404` not found / not owned · `409` duplicate name.

---

### 5.5 Delete category

| | |
|---|---|
| **Method** | `DELETE` |
| **Route** | `/api/v1/categories/{id}` |
| **Auth** | Required |

**Response — `204 No Content`**. Tasks in this category are **not** deleted; their `categoryId` is set to `null`.

**Status codes:** `204` success · `401` not authenticated · `404` not found / not owned.

---

## 6. Dashboard Statistics

### 6.1 Get dashboard stats

| | |
|---|---|
| **Method** | `GET` |
| **Route** | `/api/v1/dashboard/stats` |
| **Auth** | Required |

**Response body — `200 OK`**
```json
{
  "totals": {
    "total": 42,
    "todo": 18,
    "inProgress": 9,
    "done": 15,
    "overdue": 4
  },
  "byPriority": {
    "low": 12,
    "medium": 21,
    "high": 9
  },
  "completionRate": 35.7,
  "dueSoon": [
    {
      "id": "665f...c20",
      "title": "Finish DBMS assignment",
      "dueDate": "2026-06-25T18:30:00Z",
      "priority": "HIGH",
      "status": "IN_PROGRESS"
    }
  ],
  "generatedAt": "2026-06-22T10:15:30Z"
}
```

| Field | Meaning |
|-------|---------|
| `totals.*` | Counts of the user's tasks by status, plus overdue count. |
| `byPriority.*` | Counts by priority. |
| `completionRate` | `done / total × 100`, rounded to 1 decimal. |
| `dueSoon` | Up to 5 upcoming, not-done tasks ordered by `dueDate`. |

**Status codes:** `200` success · `401` not authenticated.

---

### 6.2 (Optional) Productivity over time

| | |
|---|---|
| **Method** | `GET` |
| **Route** | `/api/v1/dashboard/productivity` |
| **Auth** | Required |

**Query parameters:** `range` = `week` \| `month` (default `week`).

**Response body — `200 OK`**
```json
{
  "range": "week",
  "series": [
    { "date": "2026-06-16", "created": 5, "completed": 3 },
    { "date": "2026-06-17", "created": 2, "completed": 4 }
  ]
}
```

**Status codes:** `200` success · `400` invalid range · `401` not authenticated.

---

## 7. (Admin, future) Platform Metrics

| | |
|---|---|
| **Method** | `GET` |
| **Route** | `/api/v1/admin/metrics` |
| **Auth** | Required — `ADMIN` role |

**Response body — `200 OK`**
```json
{ "totalUsers": 1280, "totalTasks": 53210, "activeUsers7d": 410 }
```

**Status codes:** `200` success · `401` not authenticated · `403` not an admin.

---

## 8. Health

| | |
|---|---|
| **Method** | `GET` |
| **Route** | `/actuator/health` |
| **Auth** | None |

**Response body — `200 OK`**
```json
{ "status": "UP" }
```

Used by Railway/Vercel and uptime monitors.

---

## 9. Endpoint Summary

| Method | Route | Auth |
|--------|-------|------|
| POST | `/api/v1/auth/register` | None |
| POST | `/api/v1/auth/login` | None |
| POST | `/api/v1/auth/refresh` | None |
| POST | `/api/v1/auth/logout` | None (public) |
| GET | `/api/v1/users/me` | Required |
| PUT | `/api/v1/users/me` | Required |
| PATCH | `/api/v1/users/me/password` | Required |
| DELETE | `/api/v1/users/me` | Required |
| GET | `/api/v1/tasks` | Required |
| POST | `/api/v1/tasks` | Required |
| GET | `/api/v1/tasks/{id}` | Required |
| PUT | `/api/v1/tasks/{id}` | Required |
| PATCH | `/api/v1/tasks/{id}/status` | Required |
| DELETE | `/api/v1/tasks/{id}` | Required |
| GET | `/api/v1/categories` | Required |
| POST | `/api/v1/categories` | Required |
| GET | `/api/v1/categories/{id}` | Required |
| PUT | `/api/v1/categories/{id}` | Required |
| DELETE | `/api/v1/categories/{id}` | Required |
| GET | `/api/v1/dashboard/stats` | Required |
| GET | `/api/v1/dashboard/productivity` | Required |
| GET | `/api/v1/admin/metrics` | Admin |
| GET | `/actuator/health` | None |

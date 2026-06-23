# Software Requirements Specification (SRS)

**Project:** TaskFlow — Smart Task & Workflow Management Platform
**Document version:** 1.0
**Status:** Draft for MVP
**Last updated:** 2026-06-22

---

## 1. Project Overview

TaskFlow is a web-based task and workflow management platform designed for individuals and small teams who need to organize work without the overhead of enterprise tools. It sits deliberately between lightweight to-do apps (which lack structure and insight) and heavyweight project suites like Jira (which carry a steep learning curve).

The platform lets a user capture tasks, group them into categories, move them through a simple workflow (To Do → In Progress → Done), set priorities and deadlines, and review productivity insights through a dashboard. The system is built as a single-page React frontend backed by a stateless Spring Boot REST API and a MongoDB document store, secured with JWT-based authentication.

This document defines the requirements for the Minimum Viable Product (MVP) and records the agreed scope, user roles, requirements, and success criteria.

---

## 2. Objectives

- Provide a clean, modern interface for creating, organizing, and tracking tasks.
- Model a lightweight workflow that reflects how real work progresses, without forcing rigid process.
- Give users meaningful productivity insights (completion rate, overdue tasks, distribution by status and priority).
- Deliver a secure, stateless backend suitable for cloud deployment.
- Keep onboarding effortless — a new user should create their first task within a minute of signing up.
- Serve as a production-grade reference implementation demonstrating a full MERN-equivalent stack on Java (React + Spring Boot + MongoDB).

---

## 3. Scope

### 3.1 In Scope

- Email/password registration and login with JWT-secured sessions.
- Personal task management: create, read, update, delete (CRUD), status transitions, priorities, due dates.
- User-defined categories to group tasks, each with a name and colour.
- Filtering, sorting, and searching of tasks.
- A dashboard summarising productivity metrics.
- User profile management (name, avatar, password change, account deletion).
- Responsive UI usable on desktop and mobile browsers.

### 3.2 Out of Scope (for MVP)

- Real-time multi-user collaboration on shared boards.
- Third-party integrations (Google Calendar, Slack, email reminders).
- Native mobile applications.
- File attachments and rich-media comments.
- Team/organisation billing and subscription management.

These items are tracked under [Future Features](#10-future-features).

---

## 4. User Roles

| Role | Description | Key Permissions |
|------|-------------|-----------------|
| **Guest** | An unauthenticated visitor. | View marketing/landing pages, register, log in. |
| **User** | The primary authenticated actor. Owns their own tasks and categories. | Full CRUD on their own tasks and categories; manage own profile; view own dashboard. |
| **Admin** | Platform operator. | All User permissions plus view platform-level metrics and manage/suspend user accounts. (Backend role exists from MVP; admin UI is a future feature.) |

> **Data isolation rule:** A User can only ever access resources they own. Every task and category is scoped to its owning `userId`, enforced at the service layer, not just the UI.

---

## 5. User Stories

Stories use the format *As a `<role>`, I want `<goal>` so that `<benefit>`.*

### Authentication & Account
- As a **Guest**, I want to register with my email and password so that I can start using TaskFlow.
- As a **User**, I want to log in and stay signed in so that I don't re-authenticate on every visit.
- As a **User**, I want to update my profile and change my password so that my account stays current and secure.
- As a **User**, I want to delete my account so that my data is removed when I no longer need it.

### Tasks
- As a **User**, I want to create a task with a title, description, priority, and due date so that I can capture work clearly.
- As a **User**, I want to move a task between To Do, In Progress, and Done so that I can track its progress.
- As a **User**, I want to edit or delete a task so that I can keep my list accurate.
- As a **User**, I want to filter tasks by status, priority, and category so that I can focus on what matters now.
- As a **User**, I want to sort tasks by due date or priority so that I can plan effectively.
- As a **User**, I want to search tasks by keyword so that I can find a specific item quickly.

### Categories
- As a **User**, I want to create colour-coded categories so that I can group related tasks.
- As a **User**, I want to assign a task to a category so that my work is organised.

### Insights
- As a **User**, I want a dashboard showing how many tasks are open, in progress, completed, and overdue so that I understand my workload at a glance.
- As a **User**, I want to see my completion rate so that I can gauge my productivity over time.

### Administration
- As an **Admin**, I want to view aggregate platform usage so that I can monitor system health.

---

## 6. Functional Requirements

Requirements are identified as **FR-x** and prioritised using MoSCoW (Must / Should / Could).

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-1 | The system shall allow a guest to register with name, email, and password. | Must |
| FR-2 | The system shall reject registration with an email that already exists. | Must |
| FR-3 | The system shall authenticate users via email/password and issue a JWT access token and refresh token. | Must |
| FR-4 | The system shall allow an authenticated user to retrieve and update their profile. | Must |
| FR-5 | The system shall allow an authenticated user to change their password after re-supplying the current one. | Must |
| FR-6 | The system shall allow a user to create, read, update, and delete their own tasks. | Must |
| FR-7 | The system shall enforce that a task belongs to exactly one user and is invisible to all others. | Must |
| FR-8 | The system shall support task statuses TODO, IN_PROGRESS, and DONE, with explicit transition endpoints. | Must |
| FR-9 | The system shall support task priorities LOW, MEDIUM, and HIGH. | Must |
| FR-10 | The system shall allow optional due dates on tasks and flag tasks past their due date that are not DONE as overdue. | Must |
| FR-11 | The system shall allow a user to create, read, update, and delete their own categories. | Must |
| FR-12 | The system shall allow a task to be assigned to zero or one category. | Should |
| FR-13 | The system shall support filtering tasks by status, priority, and category. | Must |
| FR-14 | The system shall support sorting tasks by createdAt, dueDate, and priority. | Should |
| FR-15 | The system shall support keyword search across task title and description. | Should |
| FR-16 | The system shall paginate list endpoints (default 20 items per page). | Should |
| FR-17 | The system shall expose a dashboard endpoint returning task counts by status, priority, overdue count, and completion rate. | Must |
| FR-18 | The system shall set `completedAt` when a task transitions to DONE and clear it on any transition away from DONE. | Should |
| FR-19 | The system shall allow account deletion, cascading to the user's tasks and categories. | Must |
| FR-20 | The system shall expose an admin-only endpoint returning aggregate platform metrics. | Could |

---

## 7. Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-1 | **Performance** | 95th-percentile API response time under 300 ms for list/read operations on a dataset of up to 10,000 tasks per user. |
| NFR-2 | **Security** | Passwords stored using BCrypt (cost factor ≥ 10). JWTs signed with HS256/RS256; access tokens expire in 15 minutes, refresh tokens in 7 days. |
| NFR-3 | **Security** | All endpoints except auth and health are protected; every data query is scoped by authenticated `userId`. |
| NFR-4 | **Security** | Input is validated server-side (length, type, enum membership) and sanitised to prevent injection. |
| NFR-5 | **Availability** | Target 99.5% uptime on managed hosting (Railway backend, Vercel frontend). |
| NFR-6 | **Scalability** | Backend is stateless and horizontally scalable; no server-side session storage. |
| NFR-7 | **Usability** | Core flows (create task, change status) reachable in ≤ 2 clicks. Responsive down to 360 px width. |
| NFR-8 | **Maintainability** | Layered backend (controller → service → repository); ≥ 70% unit-test coverage on the service layer. |
| NFR-9 | **Portability** | Configuration via environment variables; no hard-coded secrets or endpoints. |
| NFR-10 | **Observability** | Structured request logging and a `/actuator/health` endpoint for uptime checks. |
| NFR-11 | **Compatibility** | Supports the latest two major versions of Chrome, Firefox, Safari, and Edge. |
| NFR-12 | **Data integrity** | Schema validation enforced at the MongoDB collection level in addition to application validation. |

---

## 8. MVP Features

The MVP is the smallest releasable product that delivers core value:

1. **Authentication** — register, login, refresh, logout (JWT).
2. **Profile management** — view/update profile, change password, delete account.
3. **Task CRUD** — create, list, view, edit, delete tasks.
4. **Workflow** — status transitions across TODO / IN_PROGRESS / DONE.
5. **Prioritisation & deadlines** — priority levels and due dates with overdue detection.
6. **Categories** — create colour-coded categories and assign tasks to them.
7. **Filter, sort, search, paginate** — across the task list.
8. **Dashboard** — counts by status/priority, overdue count, completion rate.
9. **Responsive UI** — usable on desktop and mobile.

---

## 9. Success Criteria

The MVP is considered successful when:

- A new user can register and create their first task in under 60 seconds.
- All Must-have functional requirements (FR-1 through FR-19 where Must) pass acceptance testing.
- The dashboard accurately reflects task state in real time after each mutation.
- The backend meets NFR-1 performance targets under a seeded 10,000-task dataset.
- Zero cross-user data leakage is observed in authorisation testing.
- The application is deployed and reachable on production URLs (Vercel + Railway) with a passing health check.
- README and documentation are complete enough for another engineer to run the project locally without assistance.

---

## 10. Future Features

Planned beyond MVP, in rough priority order:

- **Recurring tasks** and reminder notifications (email/push).
- **Subtasks and checklists** within a task.
- **Team workspaces** with shared boards and role-based access.
- **Kanban board view** with drag-and-drop status changes.
- **Calendar view** integrating due dates.
- **Tags** as a lightweight cross-cutting alternative to categories.
- **Activity log / audit trail** per task.
- **Third-party integrations** (Google Calendar, Slack).
- **Admin dashboard UI** for user and platform management.
- **Productivity trends** over time (weekly/monthly charts, streaks).
- **Export** to CSV/PDF.
- **Dark mode** and theme customisation.

---

## Appendix A — Glossary

| Term | Definition |
|------|------------|
| **Task** | A unit of work owned by a user, with a status, priority, and optional due date and category. |
| **Category** | A user-defined, colour-coded grouping of tasks. |
| **Workflow** | The lifecycle of a task across TODO → IN_PROGRESS → DONE. |
| **Overdue** | A task whose due date has passed and whose status is not DONE. |
| **Completion rate** | Completed tasks ÷ total tasks, expressed as a percentage, over a given window. |
| **JWT** | JSON Web Token used for stateless authentication. |

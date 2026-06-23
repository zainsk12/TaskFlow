<div align="center">

# 🗂️ TaskFlow

### Smart Task & Workflow Management Platform

Clean, modern task management with workflow tracking and productivity insights — without the complexity of enterprise tools or the limits of a basic to-do list.

[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=white)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite&logoColor=white)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3-06B6D4?logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7-47A248?logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](#-license)

[Live Demo](#) · [Report Bug](../../issues) · [Request Feature](../../issues)

</div>

---

## 📖 Description

**TaskFlow** is a full-stack task and workflow management platform built for students, freelancers, and small teams. It deliberately sits between lightweight to-do apps (which lack structure) and heavyweight tools like Jira (which carry a steep learning curve).

Users can capture tasks, organize them into colour-coded categories, move them through a simple workflow (**To Do → In Progress → Done**), set priorities and deadlines, and review their productivity through a clean dashboard. The backend is a **stateless, JWT-secured Spring Boot REST API** backed by **MongoDB**, and the frontend is a fast **React + Vite** single-page app.

This is a production-grade reference implementation demonstrating a complete modern stack — React on the frontend, Java/Spring Boot on the backend, and MongoDB for storage — with clean architecture, secure authentication, and cloud deployment.

---

## ✨ Features

- 🔐 **Secure authentication** — email/password sign-up and login with JWT access & refresh tokens.
- ✅ **Task management** — full CRUD with title, description, priority, due date, and category.
- 🔄 **Workflow tracking** — move tasks through To Do, In Progress, and Done with dedicated status transitions.
- 🎯 **Priorities & deadlines** — Low / Medium / High priorities and automatic overdue detection.
- 🏷️ **Categories** — create colour-coded categories to group related work.
- 🔍 **Filter, sort & search** — slice tasks by status, priority, category, or keyword; paginated lists.
- 📊 **Productivity dashboard** — counts by status and priority, overdue tracking, and completion rate.
- 👤 **Profile management** — update profile, change password, delete account (with data cascade).
- 📱 **Responsive UI** — works cleanly from mobile to desktop.
- 🛡️ **Strict data isolation** — every resource is scoped to its owner and enforced server-side.

---

## 🛠️ Tech Stack

| Layer | Technologies |
|-------|--------------|
| **Frontend** | React 18, Vite, TailwindCSS, React Router, TanStack Query, Axios |
| **Backend** | Java 21, Spring Boot 3, Spring Web, Spring Security, JWT |
| **Database** | MongoDB (Spring Data MongoDB) |
| **Auth** | JWT (access + refresh tokens), BCrypt password hashing |
| **Deployment** | Vercel (frontend), Railway (backend), MongoDB Atlas (database) |
| **Tooling** | GitHub, GitHub Actions (CI), Maven |

---

## 🏗️ Architecture Overview

```
┌──────────────┐      HTTPS / JSON + JWT      ┌─────────────────────┐
│   React SPA  │  ─────────────────────────▶  │   Spring Boot API    │
│   (Vercel)   │  ◀─────────────────────────  │   (Railway)          │
└──────────────┘                              └──────────┬──────────┘
                                                         │
                                                         ▼
                                              ┌─────────────────────┐
                                              │   MongoDB Atlas      │
                                              └─────────────────────┘
```

- **Decoupled client–server**: the React SPA and Spring Boot API deploy and scale independently, sharing only a documented REST contract.
- **Stateless backend**: identity travels in a signed JWT on every request — no server-side sessions, so the API scales horizontally.
- **Layered backend**: `Controller → Service → Repository`, with all business rules and authorization in the service layer.

📚 Full documentation lives in [`/docs`](./docs):
[SRS](./docs/SRS.md) · [Architecture](./docs/ARCHITECTURE.md) · [Database](./docs/DATABASE.md) · [API Spec](./docs/API_SPEC.md)

---

## 📸 Screenshots

> _Replace these placeholders with real screenshots/GIFs once the UI is built._

| Dashboard | Task Board | Task Detail |
|-----------|-----------|-------------|
| _`docs/screenshots/dashboard.png`_ | _`docs/screenshots/tasks.png`_ | _`docs/screenshots/task-detail.png`_ |

<div align="center">
  <em>📷 Coming soon</em>
</div>

---

## 🚀 Installation

### Prerequisites

- **Node.js** 18+ and npm
- **Java 21** (JDK)
- **Maven** 3.9+
- A **MongoDB** instance (local or [MongoDB Atlas](https://www.mongodb.com/atlas))

### Clone the repository

```bash
git clone https://github.com/<your-username>/TaskFlow.git
cd TaskFlow
```

---

## 🔑 Environment Variables

### Backend — `backend/.env` (or Railway variables)

| Variable | Description | Example |
|----------|-------------|---------|
| `MONGODB_URI` | MongoDB connection string | `mongodb+srv://user:pass@cluster.mongodb.net/taskflow` |
| `JWT_SECRET` | Secret used to sign JWTs (keep long & random) | `a-very-long-random-secret` |
| `JWT_ACCESS_EXPIRATION` | Access token lifetime (ms) | `900000` |
| `JWT_REFRESH_EXPIRATION` | Refresh token lifetime (ms) | `604800000` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origin(s) | `https://taskflow.vercel.app` |
| `SERVER_PORT` | Port the API listens on | `8080` |

### Frontend — `frontend/.env`

| Variable | Description | Example |
|----------|-------------|---------|
| `VITE_API_URL` | Base URL of the backend API | `http://localhost:8080/api/v1` |

> `.env.example` files are provided in both `frontend/` and `backend/`. Copy them to `.env` and fill in your values. **Never commit real secrets.**

---

## ▶️ Running the Backend

```bash
cd backend

# copy and edit env vars
cp .env.example .env

# run with Maven
./mvnw spring-boot:run

# or build a jar and run it
./mvnw clean package
java -jar target/taskflow-*.jar
```

The API will be available at **`http://localhost:8080`** (base path `/api/v1`).
Health check: `http://localhost:8080/actuator/health`.

---

## ▶️ Running the Frontend

```bash
cd frontend

# install dependencies
npm install

# copy and edit env vars
cp .env.example .env

# start the dev server
npm run dev
```

The app will be available at **`http://localhost:5173`** (default Vite port).

To create a production build:

```bash
npm run build
npm run preview
```

---

## 📡 API Overview

Base path: `/api/v1`. All non-auth endpoints require `Authorization: Bearer <token>`.

| Method | Route | Description |
|--------|-------|-------------|
| `POST` | `/auth/register` | Create an account |
| `POST` | `/auth/login` | Log in, receive tokens |
| `POST` | `/auth/refresh` | Refresh the access token |
| `POST` | `/auth/logout` | Invalidate refresh token |
| `GET` | `/users/me` | Get current profile |
| `PUT` | `/users/me` | Update profile |
| `PATCH` | `/users/me/password` | Change password |
| `GET` | `/tasks` | List tasks (filter/sort/search/paginate) |
| `POST` | `/tasks` | Create a task |
| `GET` | `/tasks/{id}` | Get a task |
| `PUT` | `/tasks/{id}` | Update a task |
| `PATCH` | `/tasks/{id}/status` | Change task status |
| `DELETE` | `/tasks/{id}` | Delete a task |
| `GET` | `/categories` | List categories |
| `POST` | `/categories` | Create a category |
| `PUT` | `/categories/{id}` | Update a category |
| `DELETE` | `/categories/{id}` | Delete a category |
| `GET` | `/dashboard/stats` | Productivity statistics |

📄 Full request/response details: [`docs/API_SPEC.md`](./docs/API_SPEC.md).

---

## ☁️ Deployment

| Component | Platform | Notes |
|-----------|----------|-------|
| **Frontend** | [Vercel](https://vercel.com/) | Auto-deploys from `frontend/` on push; set `VITE_API_URL` in project settings. |
| **Backend** | [Railway](https://railway.app/) | Builds the Spring Boot jar/Docker image from `backend/`; set all backend env vars. |
| **Database** | [MongoDB Atlas](https://www.mongodb.com/atlas) | Managed cluster; restrict network access to Railway. |

**Deploy steps (summary):**
1. Push to GitHub — CI runs tests on pull requests.
2. Connect the repo to Vercel (root `frontend/`) and Railway (root `backend/`).
3. Configure environment variables on each platform.
4. Set `CORS_ALLOWED_ORIGINS` on the backend to your Vercel URL.
5. Verify the API health check and the live frontend.

---

## 🗺️ Future Roadmap

- [ ] Kanban board view with drag-and-drop status changes
- [ ] Recurring tasks and reminder notifications
- [ ] Subtasks and checklists
- [ ] Team workspaces with shared boards and roles
- [ ] Calendar view integrating due dates
- [ ] Productivity trends over time (charts, streaks)
- [ ] Third-party integrations (Google Calendar, Slack)
- [ ] Admin dashboard UI
- [ ] CSV / PDF export
- [ ] Dark mode

See [`docs/SRS.md`](./docs/SRS.md#10-future-features) for the full list.

---

## 👤 Author

**[Your Name]**

- Portfolio: [your-portfolio.com](#)
- GitHub: [@your-username](https://github.com/your-username)
- LinkedIn: [your-linkedin](https://linkedin.com/in/your-username)

> _Built as a full-stack portfolio project to demonstrate clean architecture, secure JWT authentication, and modern cloud deployment._

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](../../issues).

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the **MIT License**. See [`LICENSE`](./LICENSE) for details.

```
MIT License — Copyright (c) 2026 [Your Name]
```

---

<div align="center">

⭐ If you find TaskFlow useful, consider giving it a star!

</div>
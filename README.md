<div align="center">

# 🗂️ TaskFlow

### Smart Task & Workflow Management Platform

A full-stack task and workflow management platform for organizing tasks, tracking progress, managing priorities, and monitoring productivity through a clean and responsive interface.

[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=white)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-8-646CFF?logo=vite&logoColor=white)](https://vite.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4-06B6D4?logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7-47A248?logo=mongodb&logoColor=white)](https://www.mongodb.com/)

[![Live Demo](https://img.shields.io/badge/Live-Demo-000000?logo=vercel&logoColor=white)](https://task-flow-weld-nu.vercel.app/)
[![GitHub](https://img.shields.io/badge/GitHub-Repository-181717?logo=github&logoColor=white)](https://github.com/zainsk12/TaskFlow)

</div>

---

## 📖 Description

**TaskFlow** is a full-stack task and workflow management platform built for students, freelancers, and small teams. It provides structured task management without the complexity of large enterprise project-management platforms.

Users can create and organize tasks, assign priorities and deadlines, group work into categories, move tasks through a simple workflow, apply filters, and monitor productivity through a dashboard.

The application follows a decoupled architecture with a **React + Vite frontend**, **Spring Boot REST API**, and **MongoDB** database.

---

## ✨ Features

### 🔐 Authentication & Security
- Email/password registration and login
- JWT-based authentication
- Short-lived access tokens and refresh tokens
- Refresh token stored in an **HttpOnly cookie**
- Refresh-token revocation on logout
- Password hashing with BCrypt
- Authentication endpoint rate limiting
- Server-side authorization and user data isolation

### ✅ Task Management
- Create, view, update, and delete tasks
- Task title, description, priority, due date, and category
- Task status workflow:
  - To Do
  - In Progress
  - Done
- Dedicated task status update endpoint
- Search, filtering, sorting, and pagination

### 🏷️ Organization
- Create and manage colour-coded categories
- Filter tasks by status, priority, and category
- Saved task filter presets

### 📊 Dashboard & Productivity
- Task counts by status
- Task counts by priority
- Completion rate
- Overdue task tracking
- Recent tasks
- Productivity summary

### 👤 User Management
- View and update profile
- Change password
- Delete account with associated data cleanup
- Confirm-password validation during registration

### 📱 Frontend
- Responsive interface
- React Router-based navigation
- Modern Tailwind CSS UI
- Client-side API communication with Axios
- TanStack Query for server-state management

### 🧪 Quality
- Backend unit and controller tests
- Automated frontend tests
- ESLint checks
- Production frontend build verification

---

## 🛠️ Tech Stack

| Layer | Technologies |
|-------|--------------|
| **Frontend** | React 19, Vite 8, Tailwind CSS 4, React Router, TanStack Query, Axios |
| **Backend** | Java 21, Spring Boot 4.1, Spring Web MVC, Spring Security, JJWT |
| **Database** | MongoDB with Spring Data MongoDB |
| **Authentication** | JWT, HttpOnly refresh-token cookie, BCrypt |
| **Deployment** | Vercel (frontend), Render (backend), MongoDB Atlas (database) |
| **Development** | Git, GitHub, Maven, npm |

---

## 🏗️ Architecture Overview

```text
┌──────────────────────┐
│      React SPA       │
│       Vercel         │
└──────────┬───────────┘
           │
           │ HTTPS / REST API
           ▼
┌──────────────────────┐
│    Spring Boot API   │
│       Render         │
└──────────┬───────────┘
           │
           │ Spring Data MongoDB
           ▼
┌──────────────────────┐
│    MongoDB Atlas     │
└──────────────────────┘
```

### Architecture principles

- **Decoupled client-server architecture** — frontend and backend are deployed independently.
- **REST API** — the frontend communicates with the backend through documented REST endpoints.
- **Layered backend** — `Controller → Service → Repository`.
- **Secure authentication** — JWT access tokens with refresh-token handling through an HttpOnly cookie.
- **Server-side authorization** — users can access only their own protected resources.

📚 Full documentation:

- [Software Requirements Specification](./docs/SRS.md)
- [Architecture Documentation](./docs/ARCHITECTURE.md)
- [Database Documentation](./docs/DATABASE.md)
- [API Specification](./docs/API_SPEC.md)

---

## 🌐 Live Deployment

### Frontend

**TaskFlow Web App:**  
https://task-flow-weld-nu.vercel.app/

The frontend is deployed on **Vercel** from the `frontend/` directory.

### Backend

The Spring Boot backend is deployed on **Render**.

The backend deployment uses the repository's [`render.yaml`](./render.yaml) configuration and Docker-based deployment.

### Database

The application uses **MongoDB Atlas** for the production database.

---

## 🚀 Installation

### Prerequisites

- **Node.js** 18+ and npm
- **Java 21** JDK
- **Maven** 3.9+
- MongoDB locally or a MongoDB Atlas cluster

### Clone the repository

```bash
git clone https://github.com/zainsk12/TaskFlow.git
cd TaskFlow
```

---

## 🔑 Environment Variables

### Backend

Create:

```text
backend/.env
```

Configure the required backend environment variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `MONGODB_URI` | MongoDB connection string | `mongodb+srv://user:pass@cluster.mongodb.net/taskflow` |
| `JWT_SECRET` | Long random JWT signing secret | `your-long-random-secret` |
| `JWT_ACCESS_TOKEN_EXPIRATION` | Access token lifetime | `15m` |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Refresh token lifetime | `7d` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origin | `https://task-flow-weld-nu.vercel.app` |
| `SERVER_PORT` | Local API port | `8080` |

See [`backend/.env.example`](./backend/.env.example) for the complete environment configuration.

### Frontend

Create:

```text
frontend/.env
```

Set:

```env
VITE_API_URL=http://localhost:8080/api/v1
```

For the deployed frontend, configure `VITE_API_URL` in the Vercel project settings with the deployed Render backend API URL.

See [`frontend/.env.example`](./frontend/.env.example).

> **Important:** Never commit real credentials, MongoDB connection strings, JWT secrets, or other sensitive environment values.

---

## ▶️ Running the Backend

```bash
cd backend
```

Copy the environment template:

```bash
cp .env.example .env
```

Then configure the values in `.env`.

Run the application with Maven:

```bash
./mvnw spring-boot:run
```

Or build and run the JAR:

```bash
./mvnw clean package
java -jar target/backend-*.jar
```

The local API runs on:

```text
http://localhost:8080
```

Health check:

```text
http://localhost:8080/health
```

---

## ▶️ Running the Frontend

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Create and configure `.env`, then start the development server:

```bash
npm run dev
```

The frontend will normally be available at:

```text
http://localhost:5173
```

### Production build

```bash
npm run build
```

### Lint

```bash
npm run lint
```

---

## 🧪 Testing

### Backend

From the `backend/` directory:

```bash
./mvnw test
```

### Frontend

Run the configured frontend test command when available in the project setup.

For production verification, also run:

```bash
npm run lint
npm run build
```

---

## 📡 API Overview

Base path:

```text
/api/v1
```

Authentication endpoints are public. Protected endpoints require authentication.

| Method | Route | Description |
|--------|-------|-------------|
| `POST` | `/auth/register` | Create a new account |
| `POST` | `/auth/login` | Authenticate a user |
| `POST` | `/auth/refresh` | Refresh the access token |
| `POST` | `/auth/logout` | Revoke the refresh token and clear the refresh cookie |
| `GET` | `/users/me` | Get current user profile |
| `PUT` | `/users/me` | Update profile |
| `PATCH` | `/users/me/password` | Change password |
| `GET` | `/tasks` | List tasks with filtering, sorting, search, and pagination |
| `POST` | `/tasks` | Create a task |
| `GET` | `/tasks/{id}` | Get a task |
| `PUT` | `/tasks/{id}` | Update a task |
| `PATCH` | `/tasks/{id}/status` | Change task status |
| `DELETE` | `/tasks/{id}` | Delete a task |
| `GET` | `/categories` | List categories |
| `POST` | `/categories` | Create a category |
| `PUT` | `/categories/{id}` | Update a category |
| `DELETE` | `/categories/{id}` | Delete a category |
| `GET` | `/dashboard/summary` | Get task summary and completion rate |
| `GET` | `/dashboard/status-distribution` | Get task counts by status |
| `GET` | `/dashboard/priority-distribution` | Get task counts by priority |
| `GET` | `/dashboard/recent-tasks` | Get recent tasks |
| `GET` | `/dashboard/productivity` | Get productivity information |

📄 See the complete request/response documentation in [`docs/API_SPEC.md`](./docs/API_SPEC.md).

---

## ☁️ Deployment

| Component | Platform | Configuration |
|-----------|----------|---------------|
| **Frontend** | Vercel | Root directory: `frontend/` |
| **Backend** | Render | Docker deployment using `render.yaml` |
| **Database** | MongoDB Atlas | Managed MongoDB cluster |

### Frontend deployment

The Vercel project uses:

```text
Root Directory: frontend
```

The frontend includes [`frontend/vercel.json`](./frontend/vercel.json) to support SPA deep linking. This allows routes such as `/login`, `/tasks`, and `/dashboard` to load correctly when accessed directly. Vercel documents this rewrite approach for Vite SPAs. citeturn0search0turn0search1

### Backend deployment

The Render deployment is defined in:

```text
render.yaml
```

Required production environment variables should be configured through the Render dashboard rather than committed to Git.

### Deployment flow

```text
GitHub
   │
   ├── frontend changes ──► Vercel
   │
   └── backend changes ───► Render
                              │
                              ▼
                         MongoDB Atlas
```

---

## 🗺️ Future Roadmap

- [ ] Kanban board view with drag-and-drop status changes
- [ ] Recurring tasks and reminder notifications
- [ ] Subtasks and checklists
- [ ] Team workspaces with shared boards and roles
- [ ] Calendar view integrating due dates
- [ ] Productivity trends over time
- [ ] Third-party integrations
- [ ] Admin dashboard UI
- [ ] CSV / PDF export
- [ ] Dark mode

See [`docs/SRS.md`](./docs/SRS.md) for the broader project requirements and future features.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome.

For development:

1. Create a feature branch from `main`.
2. Make your changes.
3. Test and lint the affected module.
4. Commit your changes with a clear message.
5. Push the branch to GitHub.
6. Open a Pull Request.
7. Review and merge the PR after the required checks pass.

For the current team workflow, use the repository's issue and pull-request process to coordinate changes.

---

<div align="center">

⭐ If you find TaskFlow useful, consider giving the repository a star!

</div>

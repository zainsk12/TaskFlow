// Base path of the backend API. Defaults to the relative /api/v1 which Vite
// proxies to the Spring Boot server in dev (see vite.config.js).
export const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1'

// localStorage keys for the JWT pair.
export const ACCESS_TOKEN_KEY = 'taskflow.accessToken'
export const REFRESH_TOKEN_KEY = 'taskflow.refreshToken'

// Window event dispatched when the session can no longer be recovered
// (refresh failed) so React can react and redirect to /login.
export const AUTH_LOGOUT_EVENT = 'taskflow:auth-logout'

export const TASK_STATUSES = ['TODO', 'IN_PROGRESS', 'DONE']
export const TASK_PRIORITIES = ['LOW', 'MEDIUM', 'HIGH']

export const STATUS_LABELS = {
  TODO: 'To Do',
  IN_PROGRESS: 'In Progress',
  DONE: 'Done',
}

export const PRIORITY_LABELS = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
}

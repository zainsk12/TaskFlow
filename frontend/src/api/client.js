import axios from 'axios'
import { API_BASE_URL, AUTH_LOGOUT_EVENT } from '../lib/constants'
import { getAccessToken, setAccessToken, clearAccessToken } from '../auth/tokenStorage'

// Shared axios instance. All app API calls go through here.
// `withCredentials: true` so the browser sends/accepts the HttpOnly
// refresh-token cookie (see backend CorsConfig, which allows credentials from
// the configured frontend origin(s) only — never a wildcard).
const client = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
})

// Endpoints that must never trigger a refresh-and-retry (they ARE the auth flow).
const AUTH_PATHS = ['/auth/login', '/auth/register', '/auth/refresh']

function isAuthPath(url = '') {
  return AUTH_PATHS.some((p) => url.includes(p))
}

// --- Request: attach the bearer access token -------------------------------
client.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token && !isAuthPath(config.url)) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// --- Response: transparently refresh on 401, once ---------------------------
// A single in-flight refresh is shared by all concurrent 401s (no stampede).
// The refresh token itself is never read here — it's an HttpOnly cookie the
// browser attaches automatically to this request.
let refreshPromise = null

function refreshAccessToken() {
  if (!refreshPromise) {
    refreshPromise = axios
      .post(`${API_BASE_URL}/auth/refresh`, null, { withCredentials: true })
      .then((res) => {
        const newToken = res.data?.accessToken
        setAccessToken(newToken)
        return newToken
      })
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}

function forceLogout() {
  clearAccessToken()
  window.dispatchEvent(new Event(AUTH_LOGOUT_EVENT))
}

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    const status = error.response?.status

    const canRetry =
      status === 401 &&
      original &&
      !original._retry &&
      !isAuthPath(original.url)

    if (!canRetry) {
      // A 401 on a normal request with no way to recover ends the session.
      if (status === 401 && !isAuthPath(original?.url)) {
        forceLogout()
      }
      return Promise.reject(error)
    }

    original._retry = true
    try {
      const newToken = await refreshAccessToken()
      original.headers.Authorization = `Bearer ${newToken}`
      return client(original)
    } catch (refreshError) {
      forceLogout()
      return Promise.reject(refreshError)
    }
  },
)

/** Pulls a human-readable message out of an axios error (ApiError shape from the backend). */
export function extractErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  const data = error?.response?.data
  if (data?.details?.length) {
    return data.details.map((d) => d.issue).join(', ')
  }
  return data?.message || error?.message || fallback
}

export default client

import { ACCESS_TOKEN_KEY } from '../lib/constants'

// Single source of truth for the access token. The refresh token is no
// longer handled here: it now lives in an HttpOnly cookie set by the backend
// (see docs/API_SPEC.md §2), so it is never readable from JavaScript and is
// never persisted in localStorage/sessionStorage. The browser attaches it
// automatically on requests to the backend (see src/api/client.js's
// `withCredentials: true`).

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function setAccessToken(accessToken) {
  if (accessToken) localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
}

export function clearAccessToken() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
}

export function hasAccessToken() {
  return Boolean(getAccessToken())
}

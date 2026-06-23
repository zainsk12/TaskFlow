import { ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY } from '../lib/constants'

// Single source of truth for the JWT pair. Tokens live in localStorage so a
// returning user stays signed in across reloads. (A future hardening step could
// move the access token to memory only — kept here for foundation simplicity.)

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function setTokens({ accessToken, refreshToken }) {
  if (accessToken) localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  if (refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}

export function setAccessToken(accessToken) {
  if (accessToken) localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

export function hasTokens() {
  return Boolean(getAccessToken() && getRefreshToken())
}

import client from './client'

// Auth + current-user API calls (see docs/API_SPEC.md §2 and §3.1).

export async function loginRequest({ email, password }) {
  const { data } = await client.post('/auth/login', { email, password })
  return data // { accessToken, refreshToken, tokenType, expiresIn, user }
}

export async function registerRequest({ name, email, password }) {
  const { data } = await client.post('/auth/register', { name, email, password })
  return data // same shape as login (auto-login)
}

export async function logoutRequest(refreshToken) {
  // Backend logout is a stateless 204; ignore failures (we clear locally anyway).
  try {
    await client.post('/auth/logout', { refreshToken })
  } catch {
    /* no-op */
  }
}

export async function getCurrentUser() {
  const { data } = await client.get('/users/me')
  return data // UserResponse
}

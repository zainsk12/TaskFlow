import client from './client'

// Auth + current-user API calls (see docs/API_SPEC.md §2 and §3.1).

export async function loginRequest({ email, password }) {
  const { data } = await client.post('/auth/login', { email, password })
  return data // { accessToken, tokenType, expiresIn, user } — refreshToken is an HttpOnly cookie
}

export async function registerRequest({ name, email, password }) {
  const { data } = await client.post('/auth/register', { name, email, password })
  return data // same shape as login (auto-login)
}

export async function logoutRequest() {
  // The refresh token is an HttpOnly cookie sent automatically; nothing to pass
  // in the body. Ignore failures — we clear the local session either way.
  try {
    await client.post('/auth/logout')
  } catch {
    /* no-op */
  }
}

export async function getCurrentUser() {
  const { data } = await client.get('/users/me')
  return data // UserResponse
}

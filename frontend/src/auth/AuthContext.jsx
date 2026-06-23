import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { loginRequest, registerRequest, logoutRequest } from '../api/auth'
import { setTokens, clearTokens, hasTokens, getRefreshToken } from './tokenStorage'
import { AUTH_LOGOUT_EVENT } from '../lib/constants'

const AuthContext = createContext(null)

/**
 * Owns the authentication session: the JWT pair and the login / register /
 * logout actions. It does not hold the user profile — that lives in
 * {@link UserContext}, which loads it once a session exists.
 */
export function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(() => hasTokens())

  const login = useCallback(async (credentials) => {
    const data = await loginRequest(credentials)
    setTokens(data)
    setIsAuthenticated(true)
    return data.user
  }, [])

  const register = useCallback(async (details) => {
    const data = await registerRequest(details)
    setTokens(data)
    setIsAuthenticated(true)
    return data.user
  }, [])

  const logout = useCallback(async () => {
    await logoutRequest(getRefreshToken())
    clearTokens()
    setIsAuthenticated(false)
  }, [])

  // The axios layer fires this when a refresh fails and the session is dead.
  useEffect(() => {
    const onForcedLogout = () => setIsAuthenticated(false)
    window.addEventListener(AUTH_LOGOUT_EVENT, onForcedLogout)
    return () => window.removeEventListener(AUTH_LOGOUT_EVENT, onForcedLogout)
  }, [])

  const value = useMemo(
    () => ({ isAuthenticated, login, register, logout }),
    [isAuthenticated, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}

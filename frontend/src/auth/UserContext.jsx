import { createContext, useCallback, useContext, useEffect, useMemo } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { getCurrentUser } from '../api/auth'
import { useAuth } from './AuthContext'

const UserContext = createContext(null)
const PROFILE_KEY = ['currentUser']

/**
 * Holds the authenticated user's profile. The fetch is backed by react-query and
 * only enabled while a session exists ({@code GET /users/me}); an unrecoverable
 * failure ends the session. Exposes {@code setUser} so later profile edits can
 * update the cached profile without a refetch.
 */
export function UserProvider({ children }) {
  const { isAuthenticated, logout } = useAuth()
  const queryClient = useQueryClient()

  const {
    data: user,
    isLoading,
    isError,
    refetch,
  } = useQuery({
    queryKey: PROFILE_KEY,
    queryFn: getCurrentUser,
    enabled: isAuthenticated,
    retry: false,
  })

  // A non-401 failure (401s are handled by the axios layer) ends the session so
  // we never show a half-loaded shell.
  useEffect(() => {
    if (isError) logout()
  }, [isError, logout])

  // Drop the cached profile when the session ends so a later login can't briefly
  // show the previous user.
  useEffect(() => {
    if (!isAuthenticated) queryClient.removeQueries({ queryKey: PROFILE_KEY })
  }, [isAuthenticated, queryClient])

  const setUser = useCallback(
    (next) => queryClient.setQueryData(PROFILE_KEY, next),
    [queryClient],
  )

  const refreshProfile = useCallback(async () => {
    const { data } = await refetch()
    return data
  }, [refetch])

  const value = useMemo(
    () => ({
      user: user ?? null,
      setUser,
      refreshProfile,
      // Only "loading" when a session exists and the first fetch is in flight.
      loading: isAuthenticated && isLoading,
    }),
    [user, setUser, refreshProfile, isAuthenticated, isLoading],
  )

  return <UserContext.Provider value={value}>{children}</UserContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useUser() {
  const ctx = useContext(UserContext)
  if (!ctx) throw new Error('useUser must be used within a UserProvider')
  return ctx
}

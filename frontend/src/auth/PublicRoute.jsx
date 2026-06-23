import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext'

/**
 * Inverse of ProtectedRoute: keeps already-signed-in users away from the
 * login / register pages by sending them to the dashboard.
 */
export default function PublicRoute() {
  const { isAuthenticated } = useAuth()
  if (isAuthenticated) return <Navigate to="/" replace />
  return <Outlet />
}

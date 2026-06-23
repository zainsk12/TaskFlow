import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'
import { useUser } from './UserContext'
import FullPageSpinner from '../components/ui/FullPageSpinner'

/**
 * Gate for authenticated routes. Redirects to /login when there is no session,
 * and shows a full-page spinner while the profile is loading so children never
 * render against a null user.
 */
export default function ProtectedRoute() {
  const { isAuthenticated } = useAuth()
  const { loading } = useUser()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (loading) {
    return <FullPageSpinner />
  }

  return <Outlet />
}

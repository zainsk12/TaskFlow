import { Routes, Route, Navigate } from 'react-router-dom'
import ProtectedRoute from './auth/ProtectedRoute'
import PublicRoute from './auth/PublicRoute'
import AppLayout from './components/layout/AppLayout'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import DashboardPage from './features/dashboard/DashboardPage'
import TasksPage from './features/tasks/TasksPage'
import CategoriesPage from './features/categories/CategoriesPage'

/**
 * Route map. Public auth pages live behind PublicRoute (redirect when signed
 * in); everything else is gated by ProtectedRoute and rendered in AppLayout.
 */
export default function App() {
  return (
    <Routes>
      <Route element={<PublicRoute />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/tasks" element={<TasksPage />} />
          <Route path="/categories" element={<CategoriesPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

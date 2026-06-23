import { useQueries } from '@tanstack/react-query'
import {
  getSummary,
  getStatusDistribution,
  getPriorityDistribution,
  getProductivity,
  getRecentTasks,
} from '../../api/dashboard'

/**
 * Loads every dashboard widget in parallel. Returns a flat shape so the page
 * can render each section independently while sharing one loading/error gate.
 */
export function useDashboard() {
  const results = useQueries({
    queries: [
      { queryKey: ['dashboard', 'summary'], queryFn: getSummary },
      { queryKey: ['dashboard', 'status-distribution'], queryFn: getStatusDistribution },
      { queryKey: ['dashboard', 'priority-distribution'], queryFn: getPriorityDistribution },
      { queryKey: ['dashboard', 'productivity'], queryFn: getProductivity },
      { queryKey: ['dashboard', 'recent-tasks'], queryFn: () => getRecentTasks(5) },
    ],
  })

  const [summary, statusDistribution, priorityDistribution, productivity, recentTasks] = results

  return {
    summary: summary.data,
    statusDistribution: statusDistribution.data,
    priorityDistribution: priorityDistribution.data,
    productivity: productivity.data,
    recentTasks: recentTasks.data,
    isLoading: results.some((r) => r.isLoading),
    isError: results.some((r) => r.isError),
    error: results.find((r) => r.isError)?.error,
    refetch: () => results.forEach((r) => r.refetch()),
  }
}

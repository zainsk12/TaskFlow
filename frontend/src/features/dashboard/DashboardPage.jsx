import { AlertCircle } from 'lucide-react'
import { useUser } from '../../auth/UserContext'
import { useDashboard } from './useDashboard'
import SummaryCards from './SummaryCards'
import ProductivityPanel from './ProductivityPanel'
import RecentTasks from './RecentTasks'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import FullPageSpinner from '../../components/ui/FullPageSpinner'
import StatusDonut from '../../components/charts/StatusDonut'
import PriorityBars from '../../components/charts/PriorityBars'

export default function DashboardPage() {
  const { user } = useUser()
  const {
    summary,
    statusDistribution,
    priorityDistribution,
    productivity,
    recentTasks,
    isLoading,
    isError,
    refetch,
  } = useDashboard()

  if (isLoading) return <FullPageSpinner label="Loading dashboard…" />

  if (isError) {
    return (
      <Card className="mx-auto max-w-md p-8 text-center">
        <AlertCircle className="mx-auto mb-3 h-10 w-10 text-rose-500" />
        <h2 className="text-lg font-semibold text-slate-900">Couldn’t load the dashboard</h2>
        <p className="mt-1 text-sm text-slate-500">
          There was a problem fetching your analytics. Please try again.
        </p>
        <Button className="mt-4" onClick={refetch}>
          Retry
        </Button>
      </Card>
    )
  }

  return (
    <div className="mx-auto max-w-7xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">
          Welcome back{user?.name ? `, ${user.name.split(' ')[0]}` : ''}
        </h1>
        <p className="mt-1 text-sm text-slate-500">Here’s an overview of your tasks.</p>
      </div>

      <SummaryCards summary={summary} />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card className="p-5">
          <h2 className="mb-4 text-base font-semibold text-slate-900">Status Distribution</h2>
          <StatusDonut data={statusDistribution} />
        </Card>
        <Card className="p-5">
          <h2 className="mb-4 text-base font-semibold text-slate-900">Priority Distribution</h2>
          <PriorityBars data={priorityDistribution} />
        </Card>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <ProductivityPanel data={productivity} />
        <RecentTasks tasks={recentTasks} />
      </div>
    </div>
  )
}

import { ListTodo, CheckCircle2, AlertTriangle, Flame } from 'lucide-react'
import Card from '../../components/ui/Card'
import { formatPercent } from '../../lib/format'

/**
 * Headline metric cards across the top of the dashboard.
 * {@code summary} is the DashboardSummaryResponse.
 */
export default function SummaryCards({ summary }) {
  const cards = [
    {
      label: 'Total Tasks',
      value: summary.totalTasks,
      hint: `${summary.completedTasks} completed`,
      icon: ListTodo,
      tone: 'text-brand-600 bg-brand-50',
    },
    {
      label: 'Completion Rate',
      value: formatPercent(summary.completionRate),
      hint: `${summary.inProgressTasks} in progress`,
      icon: CheckCircle2,
      tone: 'text-green-600 bg-green-50',
    },
    {
      label: 'Overdue',
      value: summary.overdueTasks,
      hint: `${summary.todoTasks} to do`,
      icon: AlertTriangle,
      tone: 'text-rose-600 bg-rose-50',
    },
    {
      label: 'High Priority',
      value: summary.highPriorityTasks,
      hint: `${summary.categoriesCount} categories`,
      icon: Flame,
      tone: 'text-amber-600 bg-amber-50',
    },
  ]

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {cards.map(({ label, value, hint, icon: Icon, tone }) => (
        <Card key={label} className="p-5">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm font-medium text-slate-500">{label}</p>
              <p className="mt-2 text-3xl font-bold text-slate-900">{value}</p>
              <p className="mt-1 text-xs text-slate-400">{hint}</p>
            </div>
            <span className={`flex h-10 w-10 items-center justify-center rounded-lg ${tone}`}>
              <Icon className="h-5 w-5" />
            </span>
          </div>
        </Card>
      ))}
    </div>
  )
}

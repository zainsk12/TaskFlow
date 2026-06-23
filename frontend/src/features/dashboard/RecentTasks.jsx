import Card from '../../components/ui/Card'
import { STATUS_LABELS, PRIORITY_LABELS } from '../../lib/constants'
import { formatDate } from '../../lib/format'

const STATUS_BADGE = {
  TODO: 'bg-slate-100 text-slate-600',
  IN_PROGRESS: 'bg-brand-50 text-brand-700',
  DONE: 'bg-green-50 text-green-700',
}

const PRIORITY_DOT = {
  LOW: 'bg-green-500',
  MEDIUM: 'bg-amber-500',
  HIGH: 'bg-rose-500',
}

/** List of the user's most recently updated tasks. */
export default function RecentTasks({ tasks = [] }) {
  return (
    <Card className="p-5">
      <h2 className="mb-4 text-base font-semibold text-slate-900">Recent Tasks</h2>

      {tasks.length === 0 ? (
        <p className="py-8 text-center text-sm text-slate-400">No tasks yet.</p>
      ) : (
        <ul className="divide-y divide-slate-100">
          {tasks.map((task) => {
            const overdue = task.isOverdue ?? task.overdue
            return (
              <li key={task.id} className="flex items-center gap-3 py-3">
                <span
                  className={`h-2.5 w-2.5 shrink-0 rounded-full ${PRIORITY_DOT[task.priority]}`}
                  title={PRIORITY_LABELS[task.priority]}
                />
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium text-slate-800">{task.title}</p>
                  <p className={`text-xs ${overdue ? 'font-medium text-rose-600' : 'text-slate-400'}`}>
                    {task.dueDate ? `Due ${formatDate(task.dueDate)}` : 'No due date'}
                    {overdue ? ' · Overdue' : ''}
                  </p>
                </div>
                <span
                  className={`shrink-0 rounded-full px-2.5 py-0.5 text-xs font-medium ${STATUS_BADGE[task.status]}`}
                >
                  {STATUS_LABELS[task.status]}
                </span>
              </li>
            )
          })}
        </ul>
      )}
    </Card>
  )
}

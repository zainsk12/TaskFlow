import { Pencil, Trash2, Calendar } from 'lucide-react'
import Card from '../../components/ui/Card'
import PriorityBadge from '../../components/ui/PriorityBadge'
import { useUpdateTask, toUpdatePayload } from './useTasks'
import { formatDate } from '../../lib/format'
import { TASK_STATUSES, STATUS_LABELS } from '../../lib/constants'

/**
 * A single task in the list. Owns its inline status dropdown (a quick status
 * update); editing and deletion are delegated to the parent via callbacks.
 */
export default function TaskRow({ task, category, onEdit, onDelete }) {
  const updateMut = useUpdateTask()
  const overdue = task.isOverdue ?? task.overdue

  function onStatusChange(e) {
    const status = e.target.value
    if (status !== task.status) {
      updateMut.mutate({ id: task.id, ...toUpdatePayload(task, { status }) })
    }
  }

  return (
    <Card className="p-4">
      <div className="flex items-start gap-3">
        <div className="min-w-0 flex-1">
          <div className="mb-1.5 flex flex-wrap items-center gap-2">
            <PriorityBadge priority={task.priority} />
            {category && (
              <span className="inline-flex items-center gap-1.5 rounded-full bg-slate-50 px-2.5 py-0.5 text-xs font-medium text-slate-600">
                <span
                  className="h-2 w-2 rounded-full"
                  style={{ backgroundColor: category.color }}
                />
                {category.name}
              </span>
            )}
          </div>

          <p className={`font-semibold text-slate-900 ${task.status === 'DONE' ? 'line-through opacity-60' : ''}`}>
            {task.title}
          </p>
          {task.description && (
            <p className="mt-0.5 line-clamp-2 text-sm text-slate-500">{task.description}</p>
          )}

          <div className="mt-2 flex flex-wrap items-center gap-3 text-xs">
            {task.dueDate && (
              <span className={`inline-flex items-center gap-1 ${overdue ? 'font-medium text-rose-600' : 'text-slate-500'}`}>
                <Calendar className="h-3.5 w-3.5" />
                {formatDate(task.dueDate)}
                {overdue ? ' · Overdue' : ''}
              </span>
            )}
            {task.tags?.length > 0 && (
              <span className="text-slate-400">
                {task.tags.map((t) => `#${t}`).join(' ')}
              </span>
            )}
          </div>
        </div>

        <div className="flex shrink-0 flex-col items-end gap-2">
          <select
            value={task.status}
            onChange={onStatusChange}
            disabled={updateMut.isPending}
            aria-label="Change status"
            className="rounded-lg border border-slate-300 bg-white py-1 pl-2 pr-7 text-xs font-medium text-slate-700 focus:border-brand-400 focus:outline-none focus:ring-2 focus:ring-brand-200 disabled:opacity-60"
          >
            {TASK_STATUSES.map((s) => (
              <option key={s} value={s}>
                {STATUS_LABELS[s]}
              </option>
            ))}
          </select>

          <div className="flex items-center gap-1">
            <button
              type="button"
              onClick={() => onEdit(task)}
              className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
              aria-label="Edit task"
            >
              <Pencil className="h-4 w-4" />
            </button>
            <button
              type="button"
              onClick={() => onDelete(task)}
              className="rounded-lg p-1.5 text-slate-400 hover:bg-rose-50 hover:text-rose-600"
              aria-label="Delete task"
            >
              <Trash2 className="h-4 w-4" />
            </button>
          </div>
        </div>
      </div>
    </Card>
  )
}

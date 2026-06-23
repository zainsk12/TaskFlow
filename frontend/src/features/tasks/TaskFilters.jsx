import { Search, X } from 'lucide-react'
import Select from '../../components/ui/Select'
import {
  TASK_STATUSES,
  TASK_PRIORITIES,
  STATUS_LABELS,
  PRIORITY_LABELS,
} from '../../lib/constants'

const STATUS_OPTIONS = TASK_STATUSES.map((s) => ({ value: s, label: STATUS_LABELS[s] }))
const PRIORITY_OPTIONS = TASK_PRIORITIES.map((p) => ({ value: p, label: PRIORITY_LABELS[p] }))

const SORT_OPTIONS = [
  { value: 'createdAt,desc', label: 'Newest first' },
  { value: 'createdAt,asc', label: 'Oldest first' },
  { value: 'dueDate,asc', label: 'Due date (soonest)' },
  { value: 'dueDate,desc', label: 'Due date (latest)' },
  { value: 'title,asc', label: 'Title (A–Z)' },
  { value: 'title,desc', label: 'Title (Z–A)' },
  { value: 'priority,desc', label: 'Priority (high–low)' },
  { value: 'priority,asc', label: 'Priority (low–high)' },
]

/**
 * Filter + sort bar for the task list. Fully controlled by {@code values} and
 * the {@code onChange} callback (called with a partial patch).
 */
export default function TaskFilters({ values, categories = [], onChange, onClear }) {
  const categoryOptions = categories.map((c) => ({ value: c.id, label: c.name }))
  const hasActiveFilters =
    values.status || values.priority || values.categoryId || values.search

  return (
    <div className="space-y-3">
      <div className="relative">
        <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
        <input
          type="search"
          value={values.search}
          onChange={(e) => onChange({ search: e.target.value })}
          placeholder="Search tasks by title or description…"
          className="block w-full rounded-lg border border-slate-300 bg-white py-2 pl-9 pr-3 text-sm text-slate-900 placeholder:text-slate-400 focus:border-brand-400 focus:outline-none focus:ring-2 focus:ring-brand-200"
        />
      </div>

      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <Select
          value={values.status}
          onChange={(e) => onChange({ status: e.target.value })}
          options={STATUS_OPTIONS}
          placeholder="All statuses"
        />
        <Select
          value={values.priority}
          onChange={(e) => onChange({ priority: e.target.value })}
          options={PRIORITY_OPTIONS}
          placeholder="All priorities"
        />
        <Select
          value={values.categoryId}
          onChange={(e) => onChange({ categoryId: e.target.value })}
          options={categoryOptions}
          placeholder="All categories"
        />
        <Select
          value={values.sort}
          onChange={(e) => onChange({ sort: e.target.value })}
          options={SORT_OPTIONS}
        />
      </div>

      {hasActiveFilters && (
        <button
          type="button"
          onClick={onClear}
          className="inline-flex items-center gap-1 text-sm font-medium text-slate-500 hover:text-slate-700"
        >
          <X className="h-4 w-4" />
          Clear filters
        </button>
      )}
    </div>
  )
}

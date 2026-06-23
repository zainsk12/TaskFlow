import { PRIORITY_LABELS } from '../../lib/constants'

const STYLES = {
  LOW: 'bg-green-50 text-green-700 ring-green-200',
  MEDIUM: 'bg-amber-50 text-amber-700 ring-amber-200',
  HIGH: 'bg-rose-50 text-rose-700 ring-rose-200',
}

/** Pill showing a task's priority. */
export default function PriorityBadge({ priority }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset ${
        STYLES[priority] || STYLES.MEDIUM
      }`}
    >
      {PRIORITY_LABELS[priority] || priority}
    </span>
  )
}

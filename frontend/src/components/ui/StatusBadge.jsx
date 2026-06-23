import { STATUS_LABELS } from '../../lib/constants'

const STYLES = {
  TODO: 'bg-slate-100 text-slate-600',
  IN_PROGRESS: 'bg-brand-50 text-brand-700',
  DONE: 'bg-green-50 text-green-700',
}

/** Pill showing a task's workflow status. */
export default function StatusBadge({ status }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
        STYLES[status] || STYLES.TODO
      }`}
    >
      {STATUS_LABELS[status] || status}
    </span>
  )
}

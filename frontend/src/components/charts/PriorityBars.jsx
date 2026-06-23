import { PRIORITY_LABELS } from '../../lib/constants'

const BARS = [
  { key: 'HIGH', color: '#f43f5e' }, // rose-500
  { key: 'MEDIUM', color: '#f59e0b' }, // amber-500
  { key: 'LOW', color: '#22c55e' }, // green-500
]

/**
 * Horizontal bar chart of task counts by priority.
 * {@code data} is { LOW, MEDIUM, HIGH }.
 */
export default function PriorityBars({ data = {} }) {
  const max = Math.max(1, ...BARS.map((b) => data[b.key] || 0))

  return (
    <ul className="flex flex-col gap-4">
      {BARS.map((b) => {
        const value = data[b.key] || 0
        const width = `${(value / max) * 100}%`
        return (
          <li key={b.key}>
            <div className="mb-1 flex items-center justify-between text-sm">
              <span className="font-medium text-slate-600">{PRIORITY_LABELS[b.key]}</span>
              <span className="font-semibold text-slate-900">{value}</span>
            </div>
            <div className="h-2.5 w-full overflow-hidden rounded-full bg-slate-100">
              <div
                className="h-full rounded-full transition-all"
                style={{ width, backgroundColor: b.color }}
              />
            </div>
          </li>
        )
      })}
    </ul>
  )
}

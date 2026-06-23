import { STATUS_LABELS } from '../../lib/constants'

// Color per status, kept consistent with the rest of the UI.
const SEGMENTS = [
  { key: 'TODO', color: '#94a3b8' }, // slate-400
  { key: 'IN_PROGRESS', color: '#6366f1' }, // brand-500
  { key: 'DONE', color: '#22c55e' }, // green-500
]

const RADIUS = 60
const STROKE = 22
const CIRCUMFERENCE = 2 * Math.PI * RADIUS

/**
 * Donut chart of task counts by status, drawn with stroke-dasharray arcs.
 * {@code data} is { TODO, IN_PROGRESS, DONE }.
 */
export default function StatusDonut({ data = {} }) {
  const total = SEGMENTS.reduce((sum, s) => sum + (data[s.key] || 0), 0)
  const divisor = total || 1

  // Each arc starts where the preceding arcs ended; computed without mutation.
  const arcs = SEGMENTS.map((s, i) => {
    const value = data[s.key] || 0
    const preceding = SEGMENTS.slice(0, i).reduce((sum, p) => sum + (data[p.key] || 0), 0)
    const dash = (value / divisor) * CIRCUMFERENCE
    return {
      ...s,
      value,
      dashArray: `${dash} ${CIRCUMFERENCE - dash}`,
      dashOffset: -((preceding / divisor) * CIRCUMFERENCE),
    }
  })

  return (
    <div className="flex flex-col items-center gap-6 sm:flex-row sm:justify-around">
      <svg width="160" height="160" viewBox="0 0 160 160" role="img" aria-label="Task status distribution">
        <g transform="rotate(-90 80 80)">
          <circle cx="80" cy="80" r={RADIUS} fill="none" stroke="#f1f5f9" strokeWidth={STROKE} />
          {total > 0 &&
            arcs.map((a) => (
              <circle
                key={a.key}
                cx="80"
                cy="80"
                r={RADIUS}
                fill="none"
                stroke={a.color}
                strokeWidth={STROKE}
                strokeDasharray={a.dashArray}
                strokeDashoffset={a.dashOffset}
                strokeLinecap="butt"
              />
            ))}
        </g>
        <text x="80" y="74" textAnchor="middle" className="fill-slate-900 text-2xl font-bold">
          {total}
        </text>
        <text x="80" y="94" textAnchor="middle" className="fill-slate-400 text-xs">
          tasks
        </text>
      </svg>

      <ul className="flex flex-col gap-2">
        {arcs.map((a) => (
          <li key={a.key} className="flex items-center gap-2 text-sm">
            <span className="h-3 w-3 rounded-sm" style={{ backgroundColor: a.color }} />
            <span className="text-slate-600">{STATUS_LABELS[a.key]}</span>
            <span className="ml-auto font-semibold text-slate-900">{a.value}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}

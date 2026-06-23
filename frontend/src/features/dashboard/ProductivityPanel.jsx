import Card from '../../components/ui/Card'
import { formatPercent } from '../../lib/format'

/**
 * Productivity metrics: completion + overdue as progress rings, active workload
 * as a number. {@code data} is the ProductivityResponse.
 */
export default function ProductivityPanel({ data }) {
  const metrics = [
    { label: 'Completion', value: data.completionPercentage, color: '#22c55e' },
    { label: 'Overdue', value: data.overduePercentage, color: '#f43f5e' },
  ]

  return (
    <Card className="p-5">
      <h2 className="mb-4 text-base font-semibold text-slate-900">Productivity</h2>
      <div className="flex items-center justify-around gap-4">
        {metrics.map((m) => (
          <Ring key={m.label} label={m.label} value={m.value} color={m.color} />
        ))}
        <div className="text-center">
          <p className="text-3xl font-bold text-slate-900">{data.activeWorkload}</p>
          <p className="mt-1 text-xs font-medium text-slate-500">Active Workload</p>
        </div>
      </div>
    </Card>
  )
}

const R = 32
const C = 2 * Math.PI * R

function Ring({ label, value, color }) {
  const pct = Math.max(0, Math.min(100, value || 0))
  const dash = (pct / 100) * C
  return (
    <div className="text-center">
      <svg width="84" height="84" viewBox="0 0 84 84">
        <circle cx="42" cy="42" r={R} fill="none" stroke="#f1f5f9" strokeWidth="8" />
        <g transform="rotate(-90 42 42)">
          <circle
            cx="42"
            cy="42"
            r={R}
            fill="none"
            stroke={color}
            strokeWidth="8"
            strokeLinecap="round"
            strokeDasharray={`${dash} ${C - dash}`}
          />
        </g>
        <text x="42" y="47" textAnchor="middle" className="fill-slate-900 text-sm font-bold">
          {formatPercent(pct)}
        </text>
      </svg>
      <p className="mt-1 text-xs font-medium text-slate-500">{label}</p>
    </div>
  )
}

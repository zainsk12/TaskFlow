import { Check } from 'lucide-react'

// A curated palette plus a free-form native picker for anything else. Values are
// 6-digit hex to satisfy the backend's color validation.
const PRESETS = [
  '#6366F1', // indigo
  '#0EA5E9', // sky
  '#22C55E', // green
  '#F59E0B', // amber
  '#F43F5E', // rose
  '#8B5CF6', // violet
  '#14B8A6', // teal
  '#EC4899', // pink
  '#64748B', // slate
  '#EF4444', // red
]

/** Swatch grid + native color input. {@code value} is a #RRGGBB string. */
export default function ColorPicker({ value, onChange, label = 'Color' }) {
  const normalized = (value || '').toUpperCase()

  return (
    <div>
      <span className="mb-1.5 block text-sm font-medium text-slate-700">{label}</span>
      <div className="flex flex-wrap items-center gap-2">
        {PRESETS.map((c) => {
          const selected = normalized === c
          return (
            <button
              key={c}
              type="button"
              onClick={() => onChange(c)}
              aria-label={c}
              aria-pressed={selected}
              className={`flex h-8 w-8 items-center justify-center rounded-full ring-2 ring-offset-2 transition ${
                selected ? 'ring-slate-400' : 'ring-transparent hover:ring-slate-200'
              }`}
              style={{ backgroundColor: c }}
            >
              {selected && <Check className="h-4 w-4 text-white" />}
            </button>
          )
        })}

        <label className="relative flex h-8 w-8 cursor-pointer items-center justify-center overflow-hidden rounded-full border border-dashed border-slate-300">
          <input
            type="color"
            value={value || '#6366F1'}
            onChange={(e) => onChange(e.target.value.toUpperCase())}
            className="absolute inset-0 h-full w-full cursor-pointer opacity-0"
            aria-label="Custom color"
          />
          <span
            className="h-5 w-5 rounded-full"
            style={{
              background:
                'conic-gradient(#f43f5e,#f59e0b,#22c55e,#0ea5e9,#6366f1,#ec4899,#f43f5e)',
            }}
          />
        </label>

        <span className="ml-1 text-sm font-mono text-slate-500">{normalized}</span>
      </div>
    </div>
  )
}

import { forwardRef, useId } from 'react'

/** Labeled textarea with inline error, matching the Input styling. */
const Textarea = forwardRef(function Textarea(
  { label, error, className = '', id, rows = 3, ...rest },
  ref,
) {
  const generatedId = useId()
  const inputId = id || generatedId

  return (
    <div className={className}>
      {label && (
        <label htmlFor={inputId} className="mb-1.5 block text-sm font-medium text-slate-700">
          {label}
        </label>
      )}
      <textarea
        ref={ref}
        id={inputId}
        rows={rows}
        aria-invalid={Boolean(error)}
        className={`block w-full rounded-lg border bg-white px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 ${
          error
            ? 'border-rose-300 focus:border-rose-400 focus:ring-rose-200'
            : 'border-slate-300 focus:border-brand-400 focus:ring-brand-200'
        }`}
        {...rest}
      />
      {error && <p className="mt-1.5 text-xs font-medium text-rose-600">{error}</p>}
    </div>
  )
})

export default Textarea

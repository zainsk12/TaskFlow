import { forwardRef, useId } from 'react'

/**
 * Labeled text input with inline error and optional leading icon. Forwards the
 * ref so it works with uncontrolled forms and focus management.
 */
const Input = forwardRef(function Input(
  { label, error, icon: Icon, className = '', id, ...rest },
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
      <div className="relative">
        {Icon && (
          <Icon
            className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
            aria-hidden="true"
          />
        )}
        <input
          ref={ref}
          id={inputId}
          aria-invalid={Boolean(error)}
          className={`block w-full rounded-lg border bg-white py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 ${
            Icon ? 'pl-9 pr-3' : 'px-3'
          } ${
            error
              ? 'border-rose-300 focus:border-rose-400 focus:ring-rose-200'
              : 'border-slate-300 focus:border-brand-400 focus:ring-brand-200'
          }`}
          {...rest}
        />
      </div>
      {error && <p className="mt-1.5 text-xs font-medium text-rose-600">{error}</p>}
    </div>
  )
})

export default Input

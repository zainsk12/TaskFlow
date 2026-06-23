import { forwardRef, useId } from 'react'
import { ChevronDown } from 'lucide-react'

/**
 * Styled native select. {@code options} is an array of { value, label }.
 * Pass {@code placeholder} to render a leading empty option.
 */
const Select = forwardRef(function Select(
  { label, error, options = [], placeholder, className = '', id, ...rest },
  ref,
) {
  const generatedId = useId()
  const selectId = id || generatedId

  return (
    <div className={className}>
      {label && (
        <label htmlFor={selectId} className="mb-1.5 block text-sm font-medium text-slate-700">
          {label}
        </label>
      )}
      <div className="relative">
        <select
          ref={ref}
          id={selectId}
          aria-invalid={Boolean(error)}
          className={`block w-full appearance-none rounded-lg border bg-white py-2 pl-3 pr-9 text-sm text-slate-900 focus:outline-none focus:ring-2 ${
            error
              ? 'border-rose-300 focus:border-rose-400 focus:ring-rose-200'
              : 'border-slate-300 focus:border-brand-400 focus:ring-brand-200'
          }`}
          {...rest}
        >
          {placeholder && <option value="">{placeholder}</option>}
          {options.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
        <ChevronDown className="pointer-events-none absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
      </div>
      {error && <p className="mt-1.5 text-xs font-medium text-rose-600">{error}</p>}
    </div>
  )
})

export default Select

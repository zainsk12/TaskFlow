import { Loader2 } from 'lucide-react'

// Tailwind can't see interpolated class names, so map to static classes.
const SIZES = {
  sm: 'h-4 w-4',
  md: 'h-5 w-5',
  lg: 'h-6 w-6',
}

/** Small inline spinner. */
export default function Spinner({ size = 'md', className = '' }) {
  return (
    <Loader2
      className={`${SIZES[size] || SIZES.md} animate-spin text-brand-600 ${className}`}
      aria-hidden="true"
    />
  )
}

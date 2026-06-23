import { Loader2 } from 'lucide-react'

/** Centered full-viewport loader used while gating protected routes. */
export default function FullPageSpinner({ label = 'Loading…' }) {
  return (
    <div className="flex h-full min-h-screen w-full flex-col items-center justify-center gap-3 bg-slate-100">
      <Loader2 className="h-8 w-8 animate-spin text-brand-600" aria-hidden="true" />
      <p className="text-sm font-medium text-slate-500">{label}</p>
    </div>
  )
}

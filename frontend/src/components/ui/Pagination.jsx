import { ChevronLeft, ChevronRight } from 'lucide-react'

/**
 * Page navigation for backend-paged lists. {@code page} is zero-based.
 * {@code onChange} receives the next zero-based page index.
 */
export default function Pagination({ page, totalPages, totalElements, size, onChange }) {
  if (totalPages <= 1) {
    return (
      <p className="text-sm text-slate-500">
        {totalElements} {totalElements === 1 ? 'task' : 'tasks'}
      </p>
    )
  }

  const from = page * size + 1
  const to = Math.min((page + 1) * size, totalElements)

  return (
    <div className="flex items-center justify-between gap-4">
      <p className="hidden text-sm text-slate-500 sm:block">
        Showing <span className="font-medium text-slate-700">{from}</span>–
        <span className="font-medium text-slate-700">{to}</span> of{' '}
        <span className="font-medium text-slate-700">{totalElements}</span>
      </p>
      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={() => onChange(page - 1)}
          disabled={page <= 0}
          className="inline-flex items-center gap-1 rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
        >
          <ChevronLeft className="h-4 w-4" />
          Prev
        </button>
        <span className="text-sm text-slate-500">
          Page {page + 1} of {totalPages}
        </span>
        <button
          type="button"
          onClick={() => onChange(page + 1)}
          disabled={page >= totalPages - 1}
          className="inline-flex items-center gap-1 rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Next
          <ChevronRight className="h-4 w-4" />
        </button>
      </div>
    </div>
  )
}

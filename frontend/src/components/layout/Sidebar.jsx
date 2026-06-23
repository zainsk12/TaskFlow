import { NavLink } from 'react-router-dom'
import { LayoutDashboard, CheckSquare, FolderKanban, X } from 'lucide-react'

// Foundation nav. Task/Category pages are wired in a later phase; their links
// are present so the shell is complete, pointing at routes added then.
const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/tasks', label: 'Tasks', icon: CheckSquare },
  { to: '/categories', label: 'Categories', icon: FolderKanban },
]

function NavItems({ onNavigate }) {
  return (
    <nav className="flex flex-col gap-1 px-3">
      {NAV_ITEMS.map(({ to, label, icon: Icon, end }) => (
        <NavLink
          key={to}
          to={to}
          end={end}
          onClick={onNavigate}
          className={({ isActive }) =>
            `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
              isActive
                ? 'bg-brand-50 text-brand-700'
                : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
            }`
          }
        >
          <Icon className="h-5 w-5" aria-hidden="true" />
          {label}
        </NavLink>
      ))}
    </nav>
  )
}

function Brand() {
  return (
    <div className="flex items-center gap-2 px-6 py-5">
      <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-600 text-base font-bold text-white">
        T
      </div>
      <span className="text-lg font-bold tracking-tight text-slate-900">TaskFlow</span>
    </div>
  )
}

/**
 * Responsive sidebar. On lg+ it is a fixed left column; below that it slides in
 * as an overlay driven by {@code open} / {@code onClose}.
 */
export default function Sidebar({ open, onClose }) {
  return (
    <>
      {/* Desktop: persistent column */}
      <aside className="hidden w-64 shrink-0 border-r border-slate-200 bg-white lg:block">
        <Brand />
        <NavItems />
      </aside>

      {/* Mobile: overlay drawer */}
      {open && (
        <div className="fixed inset-0 z-40 lg:hidden">
          <div
            className="absolute inset-0 bg-slate-900/40"
            onClick={onClose}
            aria-hidden="true"
          />
          <aside className="absolute left-0 top-0 h-full w-64 bg-white shadow-xl">
            <div className="flex items-center justify-between pr-3">
              <Brand />
              <button
                type="button"
                onClick={onClose}
                className="rounded-lg p-2 text-slate-500 hover:bg-slate-100"
                aria-label="Close menu"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            <NavItems onNavigate={onClose} />
          </aside>
        </div>
      )}
    </>
  )
}

import { useState, useRef, useEffect } from 'react'
import { Menu, LogOut, ChevronDown } from 'lucide-react'
import { useAuth } from '../../auth/AuthContext'
import { useUser } from '../../auth/UserContext'
import { initials } from '../../lib/format'

/**
 * Top navigation bar. Holds the mobile menu trigger and the user menu with
 * logout. {@code onOpenSidebar} toggles the mobile drawer.
 */
export default function Navbar({ onOpenSidebar }) {
  const { logout } = useAuth()
  const { user } = useUser()
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef(null)

  // Close the user menu when clicking outside it.
  useEffect(() => {
    function onClick(e) {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', onClick)
    return () => document.removeEventListener('mousedown', onClick)
  }, [])

  return (
    <header className="flex h-16 shrink-0 items-center justify-between border-b border-slate-200 bg-white px-4 lg:px-6">
      <button
        type="button"
        onClick={onOpenSidebar}
        className="rounded-lg p-2 text-slate-600 hover:bg-slate-100 lg:hidden"
        aria-label="Open menu"
      >
        <Menu className="h-5 w-5" />
      </button>

      {/* Spacer so the user menu stays right-aligned on desktop. */}
      <div className="hidden lg:block" />

      <div className="relative ml-auto" ref={menuRef}>
        <button
          type="button"
          onClick={() => setMenuOpen((v) => !v)}
          className="flex items-center gap-2 rounded-lg p-1.5 hover:bg-slate-100"
        >
          <span className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-100 text-sm font-semibold text-brand-700">
            {initials(user?.name)}
          </span>
          <span className="hidden text-sm font-medium text-slate-700 sm:block">
            {user?.name || 'Account'}
          </span>
          <ChevronDown className="hidden h-4 w-4 text-slate-400 sm:block" />
        </button>

        {menuOpen && (
          <div className="absolute right-0 mt-2 w-56 overflow-hidden rounded-lg border border-slate-200 bg-white shadow-lg">
            <div className="border-b border-slate-100 px-4 py-3">
              <p className="truncate text-sm font-semibold text-slate-900">{user?.name}</p>
              <p className="truncate text-xs text-slate-500">{user?.email}</p>
            </div>
            <button
              type="button"
              onClick={logout}
              className="flex w-full items-center gap-2 px-4 py-2.5 text-left text-sm text-slate-700 hover:bg-slate-50"
            >
              <LogOut className="h-4 w-4 text-slate-400" />
              Sign out
            </button>
          </div>
        )}
      </div>
    </header>
  )
}

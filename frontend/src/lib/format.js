// Small formatting helpers shared across the UI.

/** Formats an ISO instant as a short, locale-aware date (or '—' when absent). */
export function formatDate(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return '—'
  return d.toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' })
}

/** Formats an ISO instant as a relative-ish date+time for recent activity. */
export function formatDateTime(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return '—'
  return d.toLocaleString(undefined, {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/** Renders a percentage number (e.g. 33.3) as "33.3%". */
export function formatPercent(value) {
  if (value == null || Number.isNaN(value)) return '0%'
  return `${value}%`
}

/**
 * Converts an ISO instant to the value a <input type="datetime-local"> expects
 * ('YYYY-MM-DDTHH:mm' in the browser's local time). Returns '' when absent.
 */
export function isoToLocalInput(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return ''
  // Shift by the timezone offset so toISOString (UTC) yields local wall-clock time.
  const local = new Date(d.getTime() - d.getTimezoneOffset() * 60000)
  return local.toISOString().slice(0, 16)
}

/** Converts a datetime-local input value back to an ISO instant (or null when empty). */
export function localInputToIso(value) {
  if (!value) return null
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return null
  return d.toISOString()
}

/** Initials for an avatar fallback, e.g. "Ananya Sharma" -> "AS". */
export function initials(name) {
  if (!name) return '?'
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase() ?? '')
    .join('')
}

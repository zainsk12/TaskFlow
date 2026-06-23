/** Surface container with the app's standard border + soft shadow. */
export default function Card({ children, className = '', ...rest }) {
  return (
    <div
      className={`rounded-xl border border-slate-200 bg-white shadow-sm ${className}`}
      {...rest}
    >
      {children}
    </div>
  )
}

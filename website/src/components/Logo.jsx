export default function Logo({ size = 40, id = 'logo' }) {
  return (
    <svg width={size} height={size} viewBox="0 0 108 108" aria-hidden="true">
      <defs>
        <linearGradient id={`${id}-bg`} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#FF7A59" />
          <stop offset="1" stopColor="#E8386B" />
        </linearGradient>
        <linearGradient id={`${id}-coin`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#FFE38A" />
          <stop offset="1" stopColor="#F5A623" />
        </linearGradient>
      </defs>
      <rect width="108" height="108" rx="26" fill={`url(#${id}-bg)`} />
      <circle cx="92" cy="14" r="34" fill="#fff" opacity=".1" />
      <path d="M36 54h36v22q0 4-4 4H40q-4 0-4-4z" fill="#fff" />
      <path d="M54 30l24 21q2 2-1 3H31q-3-1-1-3z" fill="#FFF3EE" />
      <path d="M49.5 62h9a2.5 2.5 0 0 1 2.5 2.5V80H47V64.5a2.5 2.5 0 0 1 2.5-2.5z" fill="#E8386B" />
      <rect x="39.5" y="60" width="6" height="6" rx="1.3" fill="#FFD2C2" />
      <rect x="62.5" y="60" width="6" height="6" rx="1.3" fill="#FFD2C2" />
      <circle cx="69" cy="39" r="8.5" fill={`url(#${id}-coin)`} stroke="#fff" strokeWidth="2" />
      <path d="M69 34.1l1.4 3.1 3.5.4-2.5 2.4.7 3.4-3.1-1.7-3.1 1.7.7-3.4-2.5-2.4 3.5-.4z" fill="#fff" />
    </svg>
  )
}

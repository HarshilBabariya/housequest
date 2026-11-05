import { downloads } from '../content.js'

export default function DownloadButton({ kind, primary = false, compact = false }) {
  const d = downloads[kind]
  return (
    <a className={`btn ${primary ? 'btn-primary' : 'btn-ghost'} ${compact ? 'btn-compact' : ''}`} href={d.url} download={d.file} data-magnet>
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M12 3v12m0 0 4-4m-4 4-4-4M4 17v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2" />
      </svg>
      <span>{compact ? d.label : `Download ${d.label}`}</span>
      {!compact && <small>.apk</small>}
    </a>
  )
}

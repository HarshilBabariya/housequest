import { useEffect, useState } from 'react'
import Logo from './Logo.jsx'
import DownloadButton from './DownloadButton.jsx'
import { REPO_URL } from '../content.js'

const links = [
  ['#features', 'Features'],
  ['#festivals', 'Festivals'],
  ['#rewards', 'Rewards'],
  ['#download', 'Download']
]

export default function Nav() {
  const [solid, setSolid] = useState(false)
  useEffect(() => {
    const onScroll = () => setSolid(window.scrollY > 24)
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])
  return (
    <header className={`nav ${solid ? 'nav-solid' : ''}`}>
      <a className="brand" href="#top">
        <Logo size={34} />
        <span>HomeQuest</span>
      </a>
      <nav className="nav-links">
        {links.map(([href, label]) => <a key={href} href={href}>{label}</a>)}
        <a href={REPO_URL} target="_blank" rel="noreferrer">GitHub</a>
      </nav>
      <div className="nav-cta">
        <DownloadButton kind="lite" primary compact />
      </div>
    </header>
  )
}

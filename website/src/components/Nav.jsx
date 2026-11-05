import { useEffect, useRef, useState } from 'react'
import { animate, onScroll } from 'animejs'
import Logo from './Logo.jsx'
import DownloadButton from './DownloadButton.jsx'
import { REPO_URL } from '../content.js'

const links = [
  ['#showcase', 'Why HomeQuest'],
  ['#features', 'Features'],
  ['#festivals', 'Festivals'],
  ['#rewards', 'Rewards'],
  ['#download', 'Download']
]

export default function Nav() {
  const [solid, setSolid] = useState(false)
  const bar = useRef(null)
  useEffect(() => {
    const onScrollY = () => setSolid(window.scrollY > 24)
    onScrollY()
    window.addEventListener('scroll', onScrollY, { passive: true })
    const progress = animate(bar.current, {
      scaleX: [0, 1],
      ease: 'linear',
      autoplay: onScroll({ target: document.body, enter: 'top top', leave: 'bottom bottom', sync: true })
    })
    return () => { window.removeEventListener('scroll', onScrollY); progress.revert() }
  }, [])
  return (
    <header className={`nav ${solid ? 'nav-solid' : ''}`}>
      <i className="scroll-progress" ref={bar} />
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

import { useEffect, useRef } from 'react'
import { animate, stagger, utils } from 'animejs'
import PhoneMock from './PhoneMock.jsx'
import DownloadButton from './DownloadButton.jsx'
import { APP_VERSION, languages, stats } from '../content.js'

export default function Hero() {
  const ref = useRef(null)
  useEffect(() => {
    const root = ref.current
    animate(root.querySelectorAll('.hero-copy > *'), {
      opacity: [0, 1],
      translateY: [30, 0],
      duration: 1000,
      delay: stagger(110, { start: 150 }),
      ease: 'outExpo'
    })
    animate(root.querySelectorAll('.title-word'), {
      opacity: [0, 1],
      translateY: ['0.6em', 0],
      rotate: [4, 0],
      duration: 900,
      delay: stagger(80, { start: 250 }),
      ease: 'outBack'
    })
    const counters = root.querySelectorAll('.stat b')
    counters.forEach((el, i) => {
      const target = Number(el.dataset.value)
      const obj = { n: 0 }
      animate(obj, {
        n: target,
        duration: 1800,
        delay: 900 + i * 120,
        ease: 'outExpo',
        modifier: utils.round(0),
        onUpdate: () => { el.textContent = obj.n.toLocaleString('en-IN') }
      })
    })
    animate(root.querySelectorAll('.blob'), {
      translateX: () => utils.random(-60, 60),
      translateY: () => utils.random(-40, 40),
      scale: () => utils.random(0.9, 1.2, 2),
      duration: () => utils.random(7000, 11000),
      loop: true,
      alternate: true,
      ease: 'inOutSine'
    })
  }, [])

  return (
    <section className="hero" id="top" ref={ref}>
      <div className="blob b1" /><div className="blob b2" /><div className="blob b3" />
      <div className="hero-inner">
        <div className="hero-copy">
          <span className="eyebrow">v{APP_VERSION} · Android 8+ · {languages.join(' · ')}</span>
          <h1>
            {'Household expenses, turned into a game.'.split(' ').map((w, i) => <span className="title-word" key={i}>{w}</span>)}
          </h1>
          <p className="lead">
            Log every spend in seconds, split it with family, keep budgets — and earn points that unlock themes, avatar looks and
            festival packs. No account, no cloud: everything stays on your phone.
          </p>
          <div className="hero-cta">
            <DownloadButton kind="lite" primary />
            <DownloadButton kind="full" />
          </div>
          <p className="hero-note">Lite installs from any browser. Full adds SMS auto-capture and installs over USB. <a href="#download">Which one?</a></p>
          <ul className="stats">
            {stats.map((s) => (
              <li className="stat" key={s.label}><b data-value={s.value}>0</b><span>{s.label}</span></li>
            ))}
          </ul>
        </div>
        <div className="hero-art">
          <PhoneMock />
        </div>
      </div>
    </section>
  )
}

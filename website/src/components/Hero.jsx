import { useEffect, useRef } from 'react'
import { animate, createTimeline, stagger, utils } from 'animejs'
import PhoneMock from './PhoneMock.jsx'
import DownloadButton from './DownloadButton.jsx'
import { APP_VERSION, languages, stats } from '../content.js'
import { reducedMotion } from '../hooks/useReveal.js'

const chips = [
  { text: '₹1,240 · Groceries', sub: 'logged by voice', cls: 'k1' },
  { text: '+10 ⭐', sub: 'first log today', cls: 'k2' },
  { text: '🔥 12-day streak', sub: 'safe for today', cls: 'k3' },
  { text: 'Budget 68%', sub: '₹9,600 to spare', cls: 'k4' },
  { text: '🪔 Diwali pack', sub: 'claimed · free', cls: 'k5' }
]

export default function Hero() {
  const ref = useRef(null)
  useEffect(() => {
    const root = ref.current
    const still = reducedMotion()
    const tl = createTimeline({ defaults: { ease: 'outExpo' } })
    tl.add(root.querySelectorAll('.title-word'), { opacity: [0, 1], translateY: ['0.7em', 0], rotate: [3, 0], duration: 900, delay: stagger(70) }, 200)
      .add(root.querySelectorAll('.hero-copy > :not(h1)'), { opacity: [0, 1], translateY: [24, 0], duration: 900, delay: stagger(90) }, '-=600')
      .add(root.querySelectorAll('.chip'), { opacity: [0, 1], scale: [0.6, 1], duration: 700, delay: stagger(110), ease: 'outBack(1.6)' }, '-=500')
    root.querySelectorAll('.stat b').forEach((el, i) => {
      const obj = { n: 0 }
      animate(obj, {
        n: Number(el.dataset.value),
        duration: 1800,
        delay: 1000 + i * 120,
        ease: 'outExpo',
        modifier: utils.round(0),
        onUpdate: () => { el.textContent = obj.n.toLocaleString('en-IN') }
      })
    })
    if (still) return
    animate(root.querySelectorAll('.chip'), {
      translateY: () => [utils.random(-6, 6), utils.random(-16, 16)],
      translateX: () => [utils.random(-4, 4), utils.random(-10, 10)],
      rotate: () => [utils.random(-2, 2, 1), utils.random(-4, 4, 1)],
      duration: () => utils.random(3200, 5200),
      delay: 1500,
      loop: true,
      alternate: true,
      ease: 'inOutSine'
    })
    animate(root.querySelectorAll('.lantern'), {
      translateY: () => [0, utils.random(-220, -90)],
      translateX: () => utils.random(-30, 30),
      opacity: [{ to: 0.9, duration: 600 }, { to: 0, duration: 2400 }],
      scale: () => utils.random(0.5, 1.4, 2),
      duration: () => utils.random(3500, 6500),
      delay: stagger(260),
      loop: true,
      ease: 'outSine'
    })
    animate(root.querySelectorAll('.blob'), {
      translateX: () => utils.random(-80, 80),
      translateY: () => utils.random(-50, 50),
      scale: () => utils.random(0.85, 1.25, 2),
      duration: () => utils.random(7000, 11000),
      loop: true,
      alternate: true,
      ease: 'inOutSine'
    })
  }, [])

  return (
    <section className="hero" id="top" ref={ref}>
      <div className="blob b1" /><div className="blob b2" /><div className="blob b3" />
      <div className="lanterns" aria-hidden="true">
        {Array.from({ length: 22 }, (_, i) => <i className="lantern" key={i} style={{ left: `${(i * 41) % 100}%`, bottom: `${(i * 23) % 40}%` }} />)}
      </div>
      <div className="hero-inner">
        <div className="hero-copy">
          <span className="eyebrow pill">v{APP_VERSION} · Android 8+ · {languages.join(' · ')}</span>
          <h1>
            <span className="line"><span className="title-word">Household</span><span className="title-word">expenses,</span></span>
            <span className="line"><span className="title-word">turned</span><span className="title-word">into</span><span className="nowrap"><span className="title-word">a</span><span className="title-word grad">game.</span></span></span>
          </h1>
          <p className="lead">
            Log a spend in five seconds — by voice if you like — split it with family, keep budgets, and earn points that unlock
            themes, avatar looks and festival packs. No account, no cloud: everything stays on your phone.
          </p>
          <div className="hero-cta">
            <DownloadButton kind="lite" primary />
            <DownloadButton kind="full" />
            <a className="btn btn-text" href="#showcase">See how it works ↓</a>
          </div>
          <p className="hero-note">Free · no ads · open source · Lite installs from any browser, Full adds SMS auto-capture</p>
          <ul className="stats">
            {stats.map((s) => <li className="stat" key={s.label}><b data-value={s.value}>0</b><span>{s.label}</span></li>)}
          </ul>
        </div>
        <div className="hero-art">
          <div className="scene">
            <PhoneMock />
            {chips.map((c) => (
              <div className={`chip ${c.cls}`} key={c.cls}><b>{c.text}</b><small>{c.sub}</small></div>
            ))}
          </div>
        </div>
      </div>
    </section>
  )
}

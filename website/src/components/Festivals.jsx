import { useEffect, useRef, useState } from 'react'
import { animate, createTimeline, stagger, utils } from 'animejs'
import { festivalPerks, festivals } from '../content.js'
import { reducedMotion, useReveal } from '../hooks/useReveal.js'

function Sparks() {
  const ref = useRef(null)
  useEffect(() => {
    if (reducedMotion()) return
    animate(ref.current.querySelectorAll('i'), {
      translateY: () => [utils.random(20, 80), utils.random(-160, -40)],
      translateX: () => utils.random(-30, 30),
      opacity: [{ to: 1 }, { to: 0 }],
      scale: () => utils.random(0.4, 1.3, 2),
      duration: () => utils.random(2400, 4200),
      delay: stagger(140),
      loop: true,
      ease: 'outSine'
    })
  }, [])
  return (
    <div className="sparks" ref={ref} aria-hidden="true">
      {Array.from({ length: 28 }, (_, i) => <i key={i} style={{ left: `${(i * 37) % 100}%`, background: ['#fbbf24', '#f97316', '#ec4899', '#38bdf8'][i % 4] }} />)}
    </div>
  )
}

function SwitchDemo() {
  const ref = useRef(null)
  const [alt, setAlt] = useState(false)
  const busy = useRef(false)
  useEffect(() => {
    if (reducedMotion()) return
    animate(ref.current.querySelectorAll('.lamp'), {
      rotate: () => [utils.random(-4, -1, 1), utils.random(1, 4, 1)],
      duration: () => utils.random(1800, 2600),
      loop: true,
      alternate: true,
      ease: 'inOutSine'
    })
    animate(ref.current.querySelector('.switch-pill'), { translateY: [0, -4], duration: 1200, loop: true, alternate: true, ease: 'inOutSine' })
  }, [])
  const fly = () => {
    if (busy.current) return
    busy.current = true
    const root = ref.current
    const rocket = root.querySelector('.garland')
    const beam = root.querySelector('.beam')
    const h = root.clientHeight
    const tl = createTimeline({ onComplete: () => { busy.current = false } })
    tl.add(beam, { opacity: [0, 0.9], scaleY: [0, 1], duration: 700, ease: 'outQuad' }, 0)
      .add(rocket, { translateY: [h + 40, -220], duration: 1500, ease: 'inQuad' }, 0)
      .add(root.querySelectorAll('.spark-d'), { opacity: [0, 1, 0], translateY: [0, 120], scale: [0.4, 1.2], duration: 1500, delay: stagger(40) }, 100)
      .add(beam, { opacity: 0, duration: 600, ease: 'outQuad' }, 1100)
      .call(() => setAlt((v) => !v), 650)
  }
  return (
    <div className={`switch-demo ${alt ? 'alt' : ''}`} ref={ref} onClick={fly} role="button" tabIndex={0} onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') fly() }}>
      <div className="lamps" aria-hidden="true">{Array.from({ length: 7 }, (_, i) => <i className="lamp" key={i} style={{ left: `${8 + i * 14}%`, height: `${28 + (i * 13) % 30}px` }} />)}</div>
      <div className="demo-head">
        <span className="demo-avatar">🧑</span>
        <div><b>Harshil</b><small>⭐ 2,967 · 🔥 12</small></div>
      </div>
      <div className="demo-banner">
        <span>🪔</span>
        <div><b>Diwali in 3 days</b><small>Tap to celebrate 🎉</small></div>
      </div>
      <span className="switch-pill">🎨 Switch</span>
      <p className="demo-hint">Tap anywhere — this is what Switch (and the banner) does in the app</p>
      <i className="beam" aria-hidden="true" />
      <div className="sparks-d" aria-hidden="true">{Array.from({ length: 14 }, (_, i) => <i className="spark-d" key={i} style={{ left: `${38 + (i * 11) % 24}%`, top: `${(i * 17) % 90}%` }} />)}</div>
      <div className="garland" aria-hidden="true">
        <i className="leaf" /><i className="band" />
        <i className="ball" /><i className="ball y" /><i className="ball" /><i className="ball y" />
        <i className="tassel" />
      </div>
    </div>
  )
}

export default function Festivals() {
  const ref = useReveal('.fest-card, .perk')
  return (
    <section className="section fest" id="festivals" ref={ref}>
      <Sparks />
      <div className="section-head">
        <span className="eyebrow">Nine festival packs</span>
        <h2>The app dresses up for every festival. So does your avatar.</h2>
        <p>From three days before to three days after: a theme with two looks, hanging decor on Home, a festival app icon, an outfit, a sound and a full-screen celebration.</p>
      </div>
      <div className="fest-top">
        <SwitchDemo />
        <ul className="perks">
          {festivalPerks.map((p) => <li className="perk" key={p}><span>✦</span>{p}</li>)}
        </ul>
      </div>
      <div className="fest-grid">
        {festivals.map((f) => (
          <article className="fest-card" key={f.key} style={{ '--a': f.colors[0], '--b': f.colors[1] }}>
            <span className="fest-emoji">{f.emoji}</span>
            <h3>{f.name}</h3>
            <small>{f.when}</small>
            <dl>
              <div><dt>Theme</dt><dd>{f.theme}</dd></div>
              <div><dt>Outfit</dt><dd>{f.outfit}</dd></div>
            </dl>
          </article>
        ))}
      </div>
    </section>
  )
}

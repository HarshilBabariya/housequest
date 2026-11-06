import { useEffect, useRef, useState } from 'react'
import { animate, stagger, utils } from 'animejs'
import { festivalPerks, festivals } from '../content.js'
import { reducedMotion, useReveal } from '../hooks/useReveal.js'
import { EffectMarkup, playEffect } from './FestiveEffect.jsx'

const decorNames = {
  kites: 'Kites on strings',
  bunting: 'Tricolour bunting',
  gulal: 'Drifting colour clouds',
  toran: 'Flower toran',
  marigold: 'Marigold and mango-leaf toran',
  lamps: 'Garland with kandils and diyas',
  lights: 'Fairy lights and snow',
  stars: 'Twinkling stars'
}

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

function Decor({ kind }) {
  return (
    <div className={`decor decor-${kind}`} aria-hidden="true">
      {Array.from({ length: 7 }, (_, i) => <i key={i} style={{ left: `${8 + i * 14}%`, '--d': `${28 + (i * 13) % 30}px` }} />)}
    </div>
  )
}

function SwitchDemo({ festival }) {
  const ref = useRef(null)
  const [alt, setAlt] = useState(false)
  const busy = useRef(false)
  const look = festival.looks[alt ? 1 : 0]
  useEffect(() => { setAlt(false) }, [festival])
  useEffect(() => {
    if (reducedMotion()) return
    const sway = animate(ref.current.querySelectorAll('.decor i'), {
      rotate: () => [utils.random(-4, -1, 1), utils.random(1, 4, 1)],
      duration: () => utils.random(1800, 2600),
      loop: true,
      alternate: true,
      ease: 'inOutSine'
    })
    return () => sway.revert()
  }, [festival])
  const fly = () => {
    if (busy.current) return
    busy.current = true
    playEffect(festival.fx, ref.current.querySelector('.fx'), ref.current.clientHeight, () => setAlt((v) => !v), () => { busy.current = false })
  }
  const [p1, p2, p3, bg] = look.c
  return (
    <div className="switch-demo" style={{ '--p1': p1, '--p2': p2, '--p3': p3, '--bg1': bg }} ref={ref} onClick={fly} role="button" tabIndex={0} onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') fly() }}>
      <Decor kind={festival.decor} />
      <div className="demo-head">
        <span className="demo-avatar">🧑</span>
        <div><b>Harshil</b><small>⭐ 2,967 · 🔥 12</small></div>
      </div>
      <div className="demo-banner">
        <span>{festival.emoji}</span>
        <div><b>{festival.title.replace('!', ', Harshil!')}</b><small>{festival.line}</small></div>
      </div>
      <span className="switch-pill">🎨 Switch</span>
      <p className="demo-hint">Look: <b>{look.name}</b> · tap to switch to {festival.looks[alt ? 0 : 1].name}</p>
      <div className="fx" aria-hidden="true" key={festival.key}><EffectMarkup kind={festival.fx} colors={[p1, p2, p3]} /></div>
    </div>
  )
}

function PackDetails({ festival }) {
  const ref = useRef(null)
  useEffect(() => {
    if (reducedMotion()) return
    animate(ref.current.querySelectorAll('li'), { opacity: [0, 1], translateX: [-14, 0], duration: 500, delay: stagger(50), ease: 'outExpo' })
  }, [festival])
  const rows = [
    ['🎨', 'Two looks', `${festival.looks[0].name} ↔ ${festival.looks[1].name}`],
    ['🏮', 'Home decor', decorNames[festival.decor]],
    ['📱', 'App icon', festival.icon],
    ['👕', 'Outfit', festival.accessory ? `${festival.outfit} + ${festival.accessory}` : festival.outfit],
    ['✨', 'Celebration', festival.effect],
    ['🔊', 'Sound', festival.sound],
    ['🎁', 'Gift', '+50 ⭐ and the pack, free during the window']
  ]
  return (
    <ul className="pack-details" ref={ref}>
      {rows.map(([e, k, v]) => <li key={k}><span>{e}</span><div><small>{k}</small><b>{v}</b></div></li>)}
    </ul>
  )
}

export default function Festivals() {
  const ref = useReveal('.fest-card, .perk')
  const [key, setKey] = useState('diwali')
  const festival = festivals.find((f) => f.key === key)
  return (
    <section className="section fest" id="festivals" ref={ref}>
      <Sparks />
      <div className="section-head">
        <span className="eyebrow">Nine festival packs</span>
        <h2>The app dresses up for every festival. So does your avatar.</h2>
        <p>Pick a festival to see how Home looks that week — from three days before to three days after — and what the pack brings.</p>
      </div>
      <div className="fest-picker" role="tablist" aria-label="Festival">
        {festivals.map((f) => (
          <button type="button" role="tab" aria-selected={f.key === key} className={`fest-tab ${f.key === key ? 'on' : ''}`} key={f.key} onClick={() => setKey(f.key)}>
            <span>{f.emoji}</span>{f.name.split(' &')[0]}
          </button>
        ))}
      </div>
      <div className="fest-top">
        <SwitchDemo festival={festival} />
        <div className="fest-info">
          <h3>{festival.emoji} {festival.name} <small>{festival.when}</small></h3>
          <PackDetails festival={festival} />
        </div>
      </div>
      <ul className="perks">
        {festivalPerks.map((p) => <li className="perk" key={p}><span>✦</span>{p}</li>)}
      </ul>
      <div className="fest-grid">
        {festivals.map((f) => (
          <article className={`fest-card ${f.key === key ? 'on' : ''}`} key={f.key} style={{ '--a': f.looks[0].c[0], '--b': f.looks[0].c[2] }} onClick={() => setKey(f.key)}>
            <span className="fest-emoji">{f.emoji}</span>
            <h3>{f.name}</h3>
            <small>{f.when}</small>
            <dl>
              <div><dt>Looks</dt><dd>{f.looks[0].name} · {f.looks[1].name}</dd></div>
              <div><dt>Outfit</dt><dd>{f.outfit}</dd></div>
            </dl>
          </article>
        ))}
      </div>
    </section>
  )
}

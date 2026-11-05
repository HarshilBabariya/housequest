import { useEffect, useRef } from 'react'
import { animate, stagger, utils } from 'animejs'
import { festivalPerks, festivals } from '../content.js'
import { useReveal } from '../hooks/useReveal.js'

function Sparks() {
  const ref = useRef(null)
  useEffect(() => {
    const sparks = ref.current.querySelectorAll('i')
    animate(sparks, {
      translateY: () => [utils.random(20, 80), utils.random(-120, -40)],
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

export default function Festivals() {
  const ref = useReveal('.fest-card, .perk')
  return (
    <section className="section fest" id="festivals" ref={ref}>
      <Sparks />
      <div className="section-head">
        <span className="eyebrow">Nine festival packs</span>
        <h2>The app dresses up for every festival. So does your avatar.</h2>
        <p>Each pack brings a theme with two looks, hanging decor on Home, a festival app icon, an outfit, a sound and a full-screen celebration.</p>
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
      <ul className="perks">
        {festivalPerks.map((p) => <li className="perk" key={p}><span>✦</span>{p}</li>)}
      </ul>
    </section>
  )
}

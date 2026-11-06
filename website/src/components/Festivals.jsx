import { useEffect, useRef } from 'react'
import { animate, stagger, utils } from 'animejs'
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

export default function Festivals() {
  const ref = useReveal('.fest-card, .perk')
  return (
    <section className="section fest" id="festivals" ref={ref}>
      <Sparks />
      <div className="section-head">
        <span className="eyebrow">Nine festival packs</span>
        <h2>The app dresses up for every festival. So does your avatar.</h2>
        <p>From three days before to three days after, Home wears the festival’s colours and decor, your avatar gets the outfit, and the first open during the festival plays a celebration.</p>
      </div>
      <ul className="perks">
        {festivalPerks.map((p) => <li className="perk" key={p}><span>✦</span>{p}</li>)}
      </ul>
      <div className="fest-grid">
        {festivals.map((f) => (
          <article className="fest-card" key={f.key} style={{ '--a': f.look.c[0], '--b': f.look.c[2] }}>
            <span className="fest-emoji">{f.emoji}</span>
            <h3>{f.name}</h3>
            <small>{f.when}</small>
            <dl>
              <div><dt>Look</dt><dd><i className="swatches">{f.look.c.slice(0, 3).map((c) => <b key={c} style={{ background: c }} />)}</i>{f.look.name}</dd></div>
              <div><dt>Outfit</dt><dd>{f.outfit}</dd></div>
              <div><dt>Celebration</dt><dd>{f.effect}</dd></div>
              <div><dt>Sound</dt><dd>{f.sound}</dd></div>
            </dl>
          </article>
        ))}
      </div>
    </section>
  )
}

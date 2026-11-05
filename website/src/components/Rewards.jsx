import { useEffect, useRef } from 'react'
import { animate, stagger } from 'animejs'
import { badges, levels, points } from '../content.js'
import { useReveal } from '../hooks/useReveal.js'

export default function Rewards() {
  const ref = useReveal('.point, .level, .badge-chip', { delay: stagger(35) })
  const ladder = useRef(null)
  useEffect(() => {
    animate(ladder.current.querySelectorAll('.level i'), {
      scale: [1, 1.5],
      opacity: [0.6, 1],
      duration: 800,
      alternate: true,
      delay: stagger(160),
      loop: true,
      ease: 'inOutSine'
    })
  }, [])
  return (
    <section className="section" id="rewards" ref={ref}>
      <div className="section-head">
        <span className="eyebrow">Points, levels, badges</span>
        <h2>Every log earns something. Streaks earn more.</h2>
        <p>Spend points in the shop on 7 colour themes, 18 outfits, hair and accessories, Advanced Insights and CSV / PDF export — or claim festival packs free in season.</p>
      </div>
      <div className="rewards-grid">
        <div className="points-card">
          <h3>Earning</h3>
          <ul>
            {points.map(([v, l]) => <li className="point" key={l}><b>{v}</b><span>{l}</span></li>)}
          </ul>
        </div>
        <div className="ladder" ref={ladder}>
          <h3>Ten levels</h3>
          <ol>
            {levels.map((l, i) => <li className="level" key={l}><i /><span>{i + 1}</span>{l}</li>)}
          </ol>
        </div>
      </div>
      <div className="badges">
        {badges.map((b) => <span className="badge-chip" key={b}>{b}</span>)}
      </div>
    </section>
  )
}

import { useEffect, useRef } from 'react'
import { animate, stagger, utils } from 'animejs'
import { badges, levels, points } from '../content.js'
import { onceVisible, reducedMotion, useReveal } from '../hooks/useReveal.js'

export default function Rewards() {
  const ref = useReveal('.point, .level, .reward-xp')
  const xp = useRef(null)
  useEffect(() => {
    const root = xp.current
    const fill = root.querySelector('.xp-fill')
    const num = root.querySelector('.xp-num')
    if (reducedMotion()) { fill.style.width = '66%'; num.textContent = '1,980'; return }
    return onceVisible(root, () => {
      const obj = { n: 0 }
      animate(fill, { width: ['0%', '66%'], duration: 2000, ease: 'outExpo' })
      animate(obj, { n: 1980, duration: 2000, ease: 'outExpo', modifier: utils.round(0), onUpdate: () => { num.textContent = obj.n.toLocaleString('en-IN') } })
      animate(root.querySelectorAll('.xp-star'), { translateY: [0, -14], opacity: [0, 1, 0], scale: [0.5, 1.2], duration: 1400, delay: stagger(120, { start: 600 }), loop: true, ease: 'outQuad' })
    })
  }, [])
  return (
    <section className="section" id="rewards" ref={ref}>
      <div className="section-head">
        <span className="eyebrow">Points, levels, badges</span>
        <h2>Every log earns something. Streaks earn more.</h2>
        <p>Spend points in the shop on 7 colour themes, 18 outfits, hair and accessories, Advanced Insights and CSV / PDF export — or claim festival packs free in season.</p>
      </div>
      <div className="reward-xp" ref={xp}>
        <div className="xp-head">
          <span>Level 6 · Accountant</span>
          <span><b className="xp-num">0</b> / 3,000 XP</span>
        </div>
        <div className="xp-track"><i className="xp-fill" /></div>
        <div className="xp-stars" aria-hidden="true">{Array.from({ length: 6 }, (_, i) => <i className="xp-star" key={i} style={{ left: `${10 + i * 11}%` }}>⭐</i>)}</div>
      </div>
      <div className="rewards-grid">
        <div className="points-card">
          <h3>Earning</h3>
          <ul>
            {points.map(([v, l]) => <li className="point" key={l}><b>{v}</b><span>{l}</span></li>)}
          </ul>
        </div>
        <div className="ladder">
          <h3>Ten levels</h3>
          <ol>
            {levels.map((l, i) => <li className="level" key={l} style={{ '--i': i }}><span>{i + 1}</span>{l}</li>)}
          </ol>
        </div>
      </div>
      <div className="marquee" aria-label="Badges">
        <div className="marquee-track">
          {[...badges, ...badges].map((b, i) => <span className="badge-chip" key={i}>{b}</span>)}
        </div>
      </div>
    </section>
  )
}

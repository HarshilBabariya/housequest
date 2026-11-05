import { features } from '../content.js'
import { useReveal } from '../hooks/useReveal.js'

export default function Features() {
  const ref = useReveal('.feature', { delay: (el, i) => 40 * i })
  return (
    <section className="section" id="features" ref={ref}>
      <div className="section-head">
        <span className="eyebrow">Everything in the app</span>
        <h2>Sixteen things it does, all offline.</h2>
        <p>Kirana khata, milk and maid tallies, gold and chits, UPI auto-capture — the money of a real home, not just "Food & Transport".</p>
      </div>
      <div className="feature-grid">
        {features.map((f, i) => (
          <article className="feature" key={f.key} data-tilt style={{ '--h': `${(i * 37) % 360}` }}>
            <span className="feature-icon" aria-hidden="true">{f.emoji}</span>
            <h3>{f.title}</h3>
            <p>{f.body}</p>
          </article>
        ))}
      </div>
    </section>
  )
}

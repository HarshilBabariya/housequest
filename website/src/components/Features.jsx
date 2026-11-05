import { features } from '../content.js'
import { useReveal } from '../hooks/useReveal.js'

export default function Features() {
  const ref = useReveal('.feature')
  return (
    <section className="section" id="features" ref={ref}>
      <div className="section-head">
        <span className="eyebrow">Everything in the app</span>
        <h2>Built for an Indian household, not a spreadsheet.</h2>
        <p>Sixteen things HomeQuest does — kirana khata, UPI auto-capture, festival packs and all.</p>
      </div>
      <div className="feature-grid">
        {features.map((f) => (
          <article className="feature" key={f.key} data-tilt>
            <span className="feature-emoji" aria-hidden="true">{f.emoji}</span>
            <h3>{f.title}</h3>
            <p>{f.body}</p>
          </article>
        ))}
      </div>
    </section>
  )
}

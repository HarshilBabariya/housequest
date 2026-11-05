import { useEffect, useRef } from 'react'
import { animate, createTimeline, stagger } from 'animejs'
import { reducedMotion } from '../hooks/useReveal.js'

const bars = [38, 62, 30, 80, 54, 92, 46]
const rows = [
  ['🛒', 'Groceries', '₹1,240'],
  ['🚕', 'Auto to office', '₹120'],
  ['💡', 'Electricity bill', '₹2,310'],
  ['☕', 'Chai & samosa', '₹60']
]

export default function PhoneMock() {
  const ref = useRef(null)
  useEffect(() => {
    const root = ref.current
    const tl = createTimeline({ defaults: { ease: 'outExpo' } })
    tl.add(root, { opacity: [0, 1], translateY: [40, 0], rotateX: [12, 0], duration: 1100 })
      .add(root.querySelectorAll('.pm-fade'), { opacity: [0, 1], translateY: [12, 0], duration: 700, delay: stagger(80) }, '-=700')
      .add(root.querySelector('.pm-ring-fill'), { strokeDashoffset: [226, 226 * 0.36], duration: 1400, ease: 'inOutQuart' }, '-=500')
      .add(root.querySelectorAll('.pm-bar'), { scaleY: [0, 1], duration: 800, delay: stagger(60) }, '-=1200')
      .add(root.querySelectorAll('.pm-row'), { opacity: [0, 1], translateX: [-14, 0], duration: 600, delay: stagger(90) }, '-=900')
    if (reducedMotion()) return () => tl.cancel()
    animate(root, { translateY: [0, -8], duration: 2600, loop: true, alternate: true, ease: 'inOutSine', delay: 1200 })
    animate(root.querySelectorAll('.pm-coin'), {
      translateY: [0, -14],
      rotate: [0, 12],
      duration: 1300,
      delay: stagger(300),
      loop: true,
      alternate: true,
      ease: 'inOutSine'
    })
    animate(root.querySelector('.pm-flame'), { scale: [1, 1.18], duration: 450, loop: true, alternate: true, ease: 'inOutSine' })
    return () => tl.cancel()
  }, [])

  return (
    <div className="phone" ref={ref} aria-hidden="true">
      <span className="pm-coin c1">⭐</span>
      <span className="pm-coin c2">🪔</span>
      <span className="pm-coin c3">🪙</span>
      <div className="phone-screen">
        <div className="pm-header pm-fade">
          <div className="pm-avatar">🙂</div>
          <div>
            <div className="pm-name">Namaste, Harshil</div>
            <div className="pm-pill">⭐ 1,240 · <span className="pm-flame">🔥</span> 12</div>
          </div>
        </div>
        <div className="pm-xp pm-fade"><span>Level 6 · Accountant</span><i /></div>
        <div className="pm-banner pm-fade">
          <span>🪔</span>
          <div><b>Diwali in 3 days</b><small>Tap to celebrate 🎉</small></div>
          <em className="pm-switch">🎨 Switch</em>
        </div>
        <div className="pm-card pm-fade">
          <svg className="pm-ring" viewBox="0 0 80 80">
            <circle cx="40" cy="40" r="36" className="pm-ring-track" />
            <circle cx="40" cy="40" r="36" className="pm-ring-fill" strokeDasharray="226" strokeDashoffset="226" />
          </svg>
          <div>
            <small>Spent this month</small>
            <b>₹18,430</b>
            <small>₹11,570 left of ₹30,000</small>
          </div>
        </div>
        <div className="pm-card pm-bars pm-fade">
          <small>Last 7 days · ₹6,210</small>
          <div className="pm-bar-row">
            {bars.map((h, i) => <i key={i} className={`pm-bar ${i === 5 ? 'hot' : ''}`} style={{ height: `${h}%` }} />)}
          </div>
        </div>
        <div className="pm-list">
          {rows.map(([emoji, title, amt]) => (
            <div className="pm-row" key={title}>
              <span className="pm-row-icon">{emoji}</span>
              <span className="pm-row-title">{title}</span>
              <b>{amt}</b>
            </div>
          ))}
        </div>
        <div className="pm-nav pm-fade">
          <span>🏠</span><span>🧾</span><span className="pm-plus">+</span><span>👥</span><span>🙂</span>
        </div>
      </div>
    </div>
  )
}

import { useEffect, useRef } from 'react'
import { animate, createTimeline, stagger, utils } from 'animejs'
import { onceVisible, reducedMotion, useReveal } from '../hooks/useReveal.js'

function VoiceMock() {
  const ref = useRef(null)
  useEffect(() => {
    const root = ref.current
    const chars = root.querySelectorAll('.said span')
    const fields = root.querySelectorAll('.field')
    if (reducedMotion()) return
    return onceVisible(root, () => {
      const tl = createTimeline({ loop: true, defaults: { ease: 'outQuad' } })
      tl.add(root.querySelector('.mic'), { scale: [1, 1.15, 1], duration: 600 })
        .add(chars, { opacity: [0, 1], translateY: [6, 0], duration: 120, delay: stagger(38) }, '-=200')
        .add(fields, { opacity: [0, 1], translateX: [-16, 0], duration: 500, delay: stagger(160) }, '+=200')
        .add(root.querySelector('.save'), { scale: [0.9, 1], opacity: [0, 1], duration: 400 }, '-=100')
        .add(root.querySelector('.save'), { scale: [1, 0.95, 1], duration: 300 }, '+=900')
        .add(root.querySelector('.toast'), { opacity: [0, 1], translateY: [10, 0], duration: 400 })
        .add([chars, fields, root.querySelector('.save'), root.querySelector('.toast')], { opacity: 0, duration: 500 }, '+=1500')
    })
  }, [])
  return (
    <div className="mock voice" ref={ref}>
      <div className="mock-bar"><span>Add transaction</span><i className="mic">🎙️</i></div>
      <p className="said">{'"250 groceries by UPI, note: weekly sabzi"'.split('').map((c, i) => <span key={i}>{c}</span>)}</p>
      <div className="fields">
        <div className="field"><small>Amount</small><b>₹250</b></div>
        <div className="field"><small>Category</small><b>🛒 Groceries</b></div>
        <div className="field"><small>Paid via</small><b>UPI</b></div>
        <div className="field"><small>Note</small><b>Weekly sabzi</b></div>
      </div>
      <button className="save" type="button">Save expense · +10 ⭐</button>
      <div className="toast">🔥 Streak kept · 13 days</div>
    </div>
  )
}

function BudgetMock() {
  const ref = useRef(null)
  useEffect(() => {
    const root = ref.current
    const ring = root.querySelector('.ring-fill')
    const amount = root.querySelector('.ring-amount')
    const bars = root.querySelectorAll('.pace i')
    if (reducedMotion()) { ring.style.strokeDashoffset = 339 * 0.32; return }
    return onceVisible(root, () => {
      const obj = { n: 0 }
      animate(ring, { strokeDashoffset: [339, 339 * 0.32], duration: 1800, ease: 'inOutQuart' })
      animate(obj, { n: 20400, duration: 1800, ease: 'inOutQuart', modifier: utils.round(0), onUpdate: () => { amount.textContent = `₹${obj.n.toLocaleString('en-IN')}` } })
      animate(bars, { scaleY: [0, 1], duration: 700, delay: stagger(50, { start: 400 }), ease: 'outExpo' })
      animate(root.querySelector('.forecast'), { opacity: [0, 1], translateY: [8, 0], duration: 700, delay: 1500 })
    })
  }, [])
  return (
    <div className="mock budget" ref={ref}>
      <div className="mock-bar"><span>Cash flow · November</span><i>🎯</i></div>
      <div className="ring-row">
        <svg viewBox="0 0 120 120" className="ring">
          <circle cx="60" cy="60" r="54" className="ring-track" />
          <circle cx="60" cy="60" r="54" className="ring-fill" strokeDasharray="339" strokeDashoffset="339" />
        </svg>
        <div>
          <small>Spent of ₹30,000</small>
          <b className="ring-amount">₹0</b>
          <small>₹9,600 left · 11 days to go</small>
        </div>
      </div>
      <div className="pace">
        {[22, 38, 31, 55, 48, 70, 64, 82, 76, 92, 88, 100].map((h, i) => <i key={i} style={{ height: `${h}%` }} />)}
      </div>
      <p className="forecast">On track: about ₹3,100 to spare at this pace. Crossing day: none 🎉</p>
    </div>
  )
}

function SplitMock() {
  const ref = useRef(null)
  useEffect(() => {
    const root = ref.current
    if (reducedMotion()) return
    return onceVisible(root, () => {
      const tl = createTimeline({ loop: true, defaults: { ease: 'outExpo' } })
      tl.add(root.querySelectorAll('.member'), { opacity: [0, 1], scale: [0.6, 1], duration: 600, delay: stagger(120) })
        .add(root.querySelectorAll('.owe'), { width: (el) => ['0%', `${el.dataset.w}%`], duration: 900, delay: stagger(140) }, '-=200')
        .add(root.querySelectorAll('.owe b'), { opacity: [0, 1], duration: 300 }, '-=400')
        .add(root.querySelector('.settle'), { opacity: [0, 1], translateY: [10, 0], duration: 500 }, '+=200')
        .add(root.querySelector('.settle'), { opacity: 0, duration: 400 }, '+=2200')
        .add(root.querySelectorAll('.owe'), { width: '0%', duration: 500 }, '-=300')
        .add(root.querySelectorAll('.member'), { opacity: 0, duration: 300 }, '-=300')
    })
  }, [])
  const members = [['👩', 'Priya', 62], ['👨', 'Harshil', 40], ['🧒', 'Aarav', 18]]
  return (
    <div className="mock split" ref={ref}>
      <div className="mock-bar"><span>🏖️ Goa trip · ₹18,600</span><i>👥</i></div>
      {members.map(([e, n, w]) => (
        <div className="member-row" key={n}>
          <span className="member">{e}</span>
          <span className="member-name">{n}</span>
          <span className="owe-track"><span className="owe" data-w={w} style={{ '--w': w }}><b>₹{(w * 100).toLocaleString('en-IN')}</b></span></span>
        </div>
      ))}
      <div className="settle">Settle up: Aarav → Priya ₹2,400 · Harshil → Priya ₹1,800</div>
    </div>
  )
}

const rows = [
  {
    label: '01 · Log',
    title: 'Say it. Done.',
    body: 'Tap the mic and talk the way you talk — "paid 120 for pani puri cash", or in Hindi and Gujarati. Amount, category, payment mode and note are filled in for you. Typing is just as quick: amount hero, Yesterday / Today / Tomorrow chips, inline Paid-by and Cash · UPI · Card · Bank tiles.',
    mock: <VoiceMock />
  },
  {
    label: '02 · Budget',
    title: 'Know by the 10th how the 30th will go.',
    body: 'Set a monthly budget and per-category limits. Home shows how much is left, a month-pace chart against the budget line, and a plain-words forecast — "At this pace you\'ll overspend by ₹4,200 around 24 Nov". Alerts at 80% and 100%, and +100 points for finishing under.',
    mock: <BudgetMock />
  },
  {
    label: '03 · Split',
    title: 'Family, trips, flat — split and settled.',
    body: 'Put an expense in a group, split it equally or with custom amounts, and see exactly who owes whom. One tap gives a minimal settle-up plan; Share sends the summary to WhatsApp. Every member has their own profile, avatar and optional PIN.',
    mock: <SplitMock />
  }
]

export default function Showcase() {
  const ref = useReveal('.show-row')
  return (
    <section className="section showcase" id="showcase" ref={ref}>
      <div className="section-head">
        <span className="eyebrow">Why HomeQuest</span>
        <h2>Built for an Indian household, not a spreadsheet.</h2>
        <p>Three things it does differently from every expense tracker you've abandoned.</p>
      </div>
      {rows.map((r, i) => (
        <article className={`show-row ${i % 2 ? 'flip' : ''}`} key={r.label}>
          <div className="show-copy">
            <span className="eyebrow">{r.label}</span>
            <h3>{r.title}</h3>
            <p>{r.body}</p>
          </div>
          <div className="show-mock">{r.mock}</div>
        </article>
      ))}
    </section>
  )
}

import { createTimeline, stagger, utils } from 'animejs'

const range = (n) => Array.from({ length: n }, (_, i) => i)

export function EffectMarkup({ kind, colors }) {
  const [p1, p2, p3] = colors
  switch (kind) {
    case 'rocket':
      return (
        <>
          <div className="sparks-d">{range(14).map((i) => <i className="spark-d" key={i} style={{ left: `${38 + (i * 11) % 24}%`, top: `${(i * 17) % 90}%` }} />)}</div>
          <i className="flash" />
          <div className="burst">{range(24).map((i) => <i className="burst-p" key={i} style={{ background: [p2, p3, '#fff', p1][i % 4] }} />)}</div>
          <div className="rocket"><i className="cone" /><i className="tube"><b /></i><i className="stick" /><i className="fuse" /><i className="spark" /></div>
        </>
      )
    case 'balloons':
      return (
        <>
          {range(3).map((i) => (
            <div className="balloon" key={i} style={{ left: `${28 + i * 22}%`, '--c': colors[i % colors.length] }}><i className="splash" /><i className="bag" /><i className="knot" /></div>
          ))}
          <div className="drops">{range(30).map((i) => <i className="drop" key={i} style={{ background: colors[i % colors.length] }} />)}</div>
        </>
      )
    case 'modaks':
      return (
        <>
          <div className="petals">{range(26).map((i) => <i className="petal" key={i} style={{ left: `${(i * 37) % 100}%`, background: i % 2 ? '#fbbf24' : '#f97316' }} />)}</div>
          {range(7).map((i) => (
            <svg className="modak" key={i} viewBox="0 0 40 44" style={{ left: `${10 + i * 12}%`, width: `${30 + (i % 3) * 6}px` }}>
              <defs><radialGradient id={`mk${i}`} cx="40%" cy="35%"><stop offset="0" stopColor="#fff3c4" /><stop offset="0.6" stopColor="#fbbf24" /><stop offset="1" stopColor="#d97706" /></radialGradient></defs>
              <path d="M4 30 Q8 14 20 2 Q32 14 36 30 Q20 46 4 30 Z" fill={`url(#mk${i})`} />
              <path d="M20 4 L12 28 M20 4 L20 30 M20 4 L28 28" stroke="#b45309" strokeWidth="1.2" fill="none" opacity="0.6" />
            </svg>
          ))}
        </>
      )
    case 'kite':
      return (
        <>
          <i className="string" />
          <div className="kite" style={{ '--a': p1, '--b': p2 }}>
            <i className="sail" />
            <div className="tail">{range(5).map((i) => <i key={i} style={{ background: colors[i % colors.length] }} />)}</div>
          </div>
          <div className="sparks-d">{range(12).map((i) => <i className="spark-d" key={i} style={{ left: `${(i * 23) % 100}%`, top: `${(i * 31) % 90}%` }} />)}</div>
        </>
      )
    case 'confetti':
      return <div className="confetti">{range(60).map((i) => <i key={i} style={{ left: `${(i * 17) % 100}%`, background: ['#ff9933', '#fff', '#138808'][i % 3] }} />)}</div>
    case 'rakhi':
      return (
        <>
          <div className="petals">{range(30).map((i) => <i className="petal" key={i} style={{ left: `${(i * 37) % 100}%`, background: i % 2 ? '#f9a8d4' : '#fbcfe8' }} />)}</div>
          <div className="rakhi"><i className="thread l" /><i className="thread r" /><i className="disc">{range(12).map((i) => <b key={i} style={{ transform: `rotate(${i * 30}deg) translateY(-30px)` }} />)}<em /></i></div>
        </>
      )
    case 'dandiya':
      return (
        <>
          <div className="garba-lights">{range(16).map((i) => <i key={i} style={{ left: `${(i * 29) % 100}%`, top: `${(i * 13) % 22}%`, background: colors[i % colors.length] }} />)}</div>
          <div className="dandiya"><i className="stick l" /><i className="stick r" /><i className="clash" /></div>
        </>
      )
    case 'snow':
      return (
        <>
          <div className="snowfall">{range(50).map((i) => <i key={i} style={{ left: `${(i * 19) % 100}%`, width: `${3 + (i % 4) * 2}px`, height: `${3 + (i % 4) * 2}px` }} />)}</div>
          {[30, 70].map((x) => <div className="bell" key={x} style={{ left: `${x}%` }}><i className="rope" /><i className="body" /><i className="bow" /></div>)}
        </>
      )
    default:
      return null
  }
}

export function playEffect(kind, root, h, onFlip, onDone) {
  const tl = createTimeline({ onComplete: onDone })
  const q = (sel) => root.querySelectorAll(sel)
  switch (kind) {
    case 'rocket':
      tl.add(q('.rocket'), { translateY: [h + 60, 70], translateX: [0, 6, -6, 0], opacity: [1, 1], duration: 1300, ease: 'inOutQuad' }, 0)
        .add(q('.rocket .spark'), { scale: [0.7, 1.3], duration: 80, loop: 16, alternate: true }, 0)
        .add(q('.spark-d'), { opacity: [0, 1, 0], translateY: [0, 140], scale: [0.4, 1.1], duration: 1300, delay: stagger(50) }, 150)
        .add(q('.rocket'), { opacity: 0, duration: 80 }, 1300)
        .add(q('.flash'), { opacity: [0, 0.9, 0], scale: [0.4, 2.4], duration: 500 }, 1300)
        .add(q('.burst-p'), {
          translateX: (el, i) => [0, Math.cos((i / 24) * Math.PI * 2) * utils.random(90, 170)],
          translateY: (el, i) => [0, Math.sin((i / 24) * Math.PI * 2) * utils.random(90, 170) + 40],
          opacity: [1, 1, 0],
          scale: [1.4, 0.4],
          duration: 1300,
          ease: 'outCubic'
        }, 1320)
        .call(onFlip, 700)
      break
    case 'balloons':
      tl.add(q('.balloon'), { translateY: [h + 80, h * 0.3], translateX: [0, 10, -10, 0], opacity: [1, 1], duration: 1300, delay: stagger(140), ease: 'outQuad' }, 0)
        .add(q('.balloon .bag, .balloon .knot'), { opacity: 0, scale: 1.3, duration: 120, delay: stagger(140) }, 1250)
        .add(q('.balloon .splash'), { opacity: [0, 0.75, 0], scale: [0.2, 3], duration: 1300, delay: stagger(140), ease: 'outCubic' }, 1250)
        .add(q('.drop'), {
          translateX: (el, i) => [root.clientWidth * (0.28 + 0.22 * (i % 3)), root.clientWidth * (0.28 + 0.22 * (i % 3)) + utils.random(-120, 120)],
          translateY: (el, i) => [h * 0.3, h * 0.3 + utils.random(-80, 160)],
          opacity: [1, 0],
          scale: [1, 0.4],
          duration: 1200,
          delay: stagger(10, { start: 1300 }),
          ease: 'outQuad'
        }, 0)
        .call(onFlip, 1350)
      break
    case 'modaks':
      tl.add(q('.modak'), { translateY: [h + 60, -120], translateX: [0, 12, -12, 0], rotate: [-10, 10], opacity: [1, 1], duration: 2000, delay: stagger(60), ease: 'inOutSine' }, 0)
        .add(q('.petal'), { translateY: [-20, h + 20], translateX: () => [0, utils.random(-30, 30)], rotate: () => utils.random(180, 540), opacity: [1, 1, 0], duration: () => utils.random(1500, 2200), delay: stagger(20) }, 0)
        .call(onFlip, 900)
      break
    case 'kite':
      tl.add(q('.kite'), { translateX: [root.clientWidth * -0.2, root.clientWidth * 0.3], translateY: [h + 80, -200], rotate: [-8, 8, -4], opacity: [1, 1], duration: 2400, ease: 'inOutSine' }, 0)
        .add(q('.string'), { opacity: [0, 0.6, 0], duration: 2400 }, 0)
        .add(q('.kite .tail i'), { rotate: [-20, 20], duration: 220, loop: 4, alternate: true, delay: stagger(60) }, 0)
        .add(q('.spark-d'), { opacity: [0, 1, 0], translateY: [0, 60], duration: 1000, delay: stagger(80) }, 200)
        .call(onFlip, 1000)
      break
    case 'confetti':
      tl.add(q('.confetti i'), { translateY: [-30, h + 40], translateX: () => [0, utils.random(-40, 40)], rotate: () => utils.random(360, 1080), opacity: [1, 1, 0], duration: () => utils.random(1400, 2000), delay: stagger(10) }, 0)
        .call(onFlip, 700)
      break
    case 'rakhi':
      tl.add(q('.rakhi'), { translateY: [h + 80, h * 0.3], translateX: [0, 8, -8, 0], opacity: [1, 1, 1, 0], duration: 2400, ease: 'outQuad' }, 0)
        .add(q('.rakhi .disc'), { rotate: [0, 180], duration: 2400, ease: 'inOutSine' }, 0)
        .add(q('.petal'), { translateY: [-20, h + 20], translateX: () => [0, utils.random(-40, 40)], rotate: () => utils.random(180, 540), opacity: [1, 1, 0], duration: () => utils.random(1500, 2200), delay: stagger(15) }, 0)
        .call(onFlip, 900)
      break
    case 'dandiya':
      tl.add(q('.dandiya'), { translateY: [h + 120, h * 0.28], opacity: [1, 1, 1, 0], duration: 2400, ease: 'outQuad' }, 0)
        .add(q('.dandiya .stick.l'), { rotate: [-50, -20], duration: 300, loop: 3, alternate: true }, 0)
        .add(q('.dandiya .stick.r'), { rotate: [50, 20], duration: 300, loop: 3, alternate: true }, 0)
        .add(q('.dandiya .clash'), { opacity: [0, 1, 0], scale: [0.4, 1.6], duration: 600, loop: 3 }, 300)
        .add(q('.garba-lights i'), { opacity: [0.2, 1], scale: [0.8, 1.4], duration: 320, loop: 3, alternate: true, delay: stagger(20) }, 0)
        .call(onFlip, 900)
      break
    case 'snow':
      tl.add(q('.snowfall i'), { translateY: [-20, h + 20], translateX: () => [0, utils.random(-30, 30)], opacity: [0, 1, 1, 0], duration: () => utils.random(1500, 2200), delay: stagger(8) }, 0)
        .add(q('.bell'), { rotate: [-18, 18], opacity: [1, 1], duration: 500, loop: 2, alternate: true, ease: 'inOutSine' }, 0)
        .add(q('.bell'), { opacity: 0, duration: 300 }, 2200)
        .call(onFlip, 900)
      break
    default:
      onDone()
  }
  return tl
}

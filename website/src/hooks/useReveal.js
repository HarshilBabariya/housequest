import { useEffect, useRef } from 'react'
import { animate, stagger } from 'animejs'

export const reducedMotion = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches

export function useReveal(selector, options = {}) {
  const ref = useRef(null)
  useEffect(() => {
    const root = ref.current
    const targets = root.querySelectorAll(selector)
    if (!targets.length || reducedMotion()) return
    targets.forEach((el) => { el.style.opacity = '0' })
    const observer = new IntersectionObserver((entries) => {
      if (!entries.some((e) => e.isIntersecting)) return
      observer.disconnect()
      animate(targets, {
        opacity: [0, 1],
        translateY: [36, 0],
        scale: [0.97, 1],
        duration: 1000,
        delay: stagger(70),
        ease: 'outExpo',
        ...options,
        onComplete: () => targets.forEach((el) => { el.style.opacity = ''; el.style.transform = '' })
      })
    }, { threshold: 0.12 })
    observer.observe(root)
    return () => observer.disconnect()
  }, [])
  return ref
}

export function onceVisible(el, run, threshold = 0.35) {
  const observer = new IntersectionObserver((entries) => {
    if (!entries.some((e) => e.isIntersecting)) return
    observer.disconnect()
    run()
  }, { threshold })
  observer.observe(el)
  return () => observer.disconnect()
}

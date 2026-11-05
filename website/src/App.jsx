import { useEffect } from 'react'
import { animate } from 'animejs'
import DownloadButton from './components/DownloadButton.jsx'
import Nav from './components/Nav.jsx'
import Hero from './components/Hero.jsx'
import Showcase from './components/Showcase.jsx'
import Features from './components/Features.jsx'
import Festivals from './components/Festivals.jsx'
import Rewards from './components/Rewards.jsx'
import Downloads from './components/Downloads.jsx'
import Footer from './components/Footer.jsx'

function useMagnet() {
  useEffect(() => {
    const fine = window.matchMedia('(hover: hover) and (pointer: fine)').matches
    if (!fine) return
    const move = (e) => {
      const el = e.currentTarget
      const r = el.getBoundingClientRect()
      animate(el, { translateX: (e.clientX - r.left - r.width / 2) * 0.18, translateY: (e.clientY - r.top - r.height / 2) * 0.3, duration: 300, ease: 'outQuad' })
    }
    const leave = (e) => animate(e.currentTarget, { translateX: 0, translateY: 0, duration: 600, ease: 'outElastic(1, .5)' })
    const els = document.querySelectorAll('[data-magnet]')
    els.forEach((el) => { el.addEventListener('mousemove', move); el.addEventListener('mouseleave', leave) })
    return () => els.forEach((el) => { el.removeEventListener('mousemove', move); el.removeEventListener('mouseleave', leave) })
  }, [])
}

function useTilt() {
  useEffect(() => {
    const fine = window.matchMedia('(hover: hover) and (pointer: fine)').matches
    if (!fine) return
    const move = (e) => {
      const el = e.currentTarget
      const r = el.getBoundingClientRect()
      const x = (e.clientX - r.left) / r.width - 0.5
      const y = (e.clientY - r.top) / r.height - 0.5
      animate(el, { rotateY: x * 10, rotateX: -y * 10, translateY: -4, duration: 250, ease: 'outQuad' })
    }
    const leave = (e) => animate(e.currentTarget, { rotateY: 0, rotateX: 0, translateY: 0, duration: 500, ease: 'outQuad' })
    const els = document.querySelectorAll('[data-tilt]')
    els.forEach((el) => { el.addEventListener('mousemove', move); el.addEventListener('mouseleave', leave) })
    return () => els.forEach((el) => { el.removeEventListener('mousemove', move); el.removeEventListener('mouseleave', leave) })
  }, [])
}

export default function App() {
  useMagnet()
  useTilt()
  return (
    <>
      <Nav />
      <main>
        <Hero />
        <Showcase />
        <Features />
        <Festivals />
        <Rewards />
        <Downloads />
      </main>
      <Footer />
      <div className="sticky-cta"><span>HomeQuest for Android</span><DownloadButton kind="lite" primary compact /></div>
    </>
  )
}

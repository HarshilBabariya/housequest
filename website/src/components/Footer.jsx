import Logo from './Logo.jsx'
import { APP_VERSION, REPO_URL } from '../content.js'

export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer-brand">
        <Logo size={28} id="footer-logo" />
        <span>HomeQuest {APP_VERSION}</span>
      </div>
      <p>Native Android · Kotlin · Jetpack Compose · Material 3 · Room. Your data never leaves the phone.</p>
      <nav>
        <a href={REPO_URL} target="_blank" rel="noreferrer">Source on GitHub</a>
        <a href={`${REPO_URL}/issues`} target="_blank" rel="noreferrer">Report a bug</a>
        <a href={`${REPO_URL}/releases`} target="_blank" rel="noreferrer">Releases</a>
      </nav>
    </footer>
  )
}

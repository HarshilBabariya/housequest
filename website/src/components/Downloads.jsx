import { APP_VERSION, builds, installSteps, REPO_URL } from '../content.js'
import DownloadButton from './DownloadButton.jsx'
import { useReveal } from '../hooks/useReveal.js'

export default function Downloads() {
  const ref = useReveal('.build, .step')
  return (
    <section className="section download" id="download" ref={ref}>
      <div className="section-head">
        <span className="eyebrow">Get HomeQuest {APP_VERSION}</span>
        <h2>Two builds, one app.</h2>
        <p>Both share the same app id, so either installs over the other and keeps your data. Free, no ads, no account, open source.</p>
      </div>
      <div className="builds">
        {builds.map((b) => (
          <article className={`build build-${b.key}`} key={b.key}>
            {b.key === 'lite' && <span className="ribbon">Recommended</span>}
            <h3>{b.title}</h3>
            <p>{b.tagline}</p>
            <ul>{b.rows.map((r) => <li key={r}>{r}</li>)}</ul>
            <DownloadButton kind={b.key} primary={b.key === 'lite'} />
          </article>
        ))}
      </div>
      <ol className="steps">
        {installSteps.map(([t, body], i) => (
          <li className="step" key={t}><span>{i + 1}</span><div><h4>{t}</h4><p>{body}</p></div></li>
        ))}
      </ol>
      <p className="download-foot">
        Releases are built by GitHub Actions from the <a href={REPO_URL} target="_blank" rel="noreferrer">open source</a>. Want a specific version? See <a href={`${REPO_URL}/releases`} target="_blank" rel="noreferrer">all releases</a>.
      </p>
    </section>
  )
}

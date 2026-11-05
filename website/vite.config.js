import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { readFileSync } from 'node:fs'

const gradle = readFileSync(new URL('../app/build.gradle.kts', import.meta.url), 'utf8')
const version = gradle.match(/versionName = "([^"]+)"/)[1]

export default defineConfig({
  base: '/housequest/',
  plugins: [react()],
  define: { __APP_VERSION__: JSON.stringify(version) }
})

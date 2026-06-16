import { createElement } from 'react'
import { createRoot } from 'react-dom/client'
import ServerView from './ServerView'
import { registerWidget } from './widgets'

// This file is bundled (with React) into a single script that the plugin embeds in its
// jar and serves automatically. The page needs nothing else: it reads window.__VIEW__
// and mounts the matching Java/Kotlin Server Component. Like Thymeleaf, the consumer
// ships no frontend — they just write server components.

// Optional API for custom widgets, exposed globally for advanced users.
;(window as any).SpringReact = { registerWidget }

function mount() {
  const el = document.getElementById('root')
  if (!el) return
  const view = (window as any).__VIEW__ as string
  createRoot(el).render(createElement(ServerView, { name: view }))
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', mount)
} else {
  mount()
}

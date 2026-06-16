import { createElement } from 'react'
import { createRoot } from 'react-dom/client'
import Router, { navigate } from './Router'
import { registerWidget } from './widgets'

// Bundled (with React) into a single script the framework embeds in its jar and serves
// automatically. The page needs nothing else: this reads window.__VIEW__ / __ROUTES__
// and mounts the matching server component, with client-side navigation between routes.

;(window as any).SpringReact = { registerWidget, navigate }

function mount() {
  const el = document.getElementById('root')
  if (!el) return
  createRoot(el).render(createElement(Router))
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', mount)
} else {
  mount()
}

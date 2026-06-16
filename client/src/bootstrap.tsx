import * as React from 'react'
import { createElement } from 'react'
import * as ReactDOMClient from 'react-dom/client'
import * as JsxRuntime from 'react/jsx-runtime'
import Router, { navigate } from './Router'
import { registerWidget } from './widgets'

// Bundled (with React) into a single script the framework embeds in its jar and serves
// automatically. The page needs nothing else: this reads window.__VIEW__ / __ROUTES__
// and mounts the matching server component, with client-side navigation between routes.
//
// We expose React/ReactDOM/jsx-runtime on window.SpringReact so consumer "widget" bundles
// (charts, three.js, etc.) can SHARE this React instance instead of bundling their own —
// avoiding the "two copies of React / invalid hook call" problem. A widget bundle marks
// react/react-dom as external and points them at these globals (see docs).

;(window as any).SpringReact = {
  registerWidget,
  navigate,
  React,
  ReactDOMClient,
  JsxRuntime,
}

function mount() {
  const el = document.getElementById('root')
  if (!el) return
  ReactDOMClient.createRoot(el).render(createElement(Router))
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', mount)
} else {
  mount()
}

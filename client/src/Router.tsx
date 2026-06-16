import { createElement, useEffect, useState, type ReactNode } from 'react'
import ServerView from './ServerView'
import { resolveRoute, type Routes } from './routing'

function routes(): Routes {
  return (window as any).__ROUTES__ ?? {}
}

/**
 * Client-side router. Intercepts internal link clicks and back/forward, swaps the screen
 * over the live WebSocket (no full reload), and persists a layout across navigation: the
 * layout component stays mounted while only the inner screen (keyed by view) remounts.
 */
export default function Router() {
  const [path, setPath] = useState(() => location.pathname)

  useEffect(() => {
    const onPop = () => setPath(location.pathname)
    const onNavigate = (e: Event) => setPath((e as CustomEvent).detail as string)
    const onClick = (e: MouseEvent) => {
      if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return
      const anchor = (e.target as HTMLElement)?.closest?.('a')
      const href = anchor?.getAttribute('href')
      if (!href || !href.startsWith('/') || anchor?.getAttribute('target')) return
      if (!(href in routes())) return // let the browser handle non-app links
      e.preventDefault()
      if (href !== location.pathname) history.pushState({}, '', href)
      setPath(href)
    }
    window.addEventListener('popstate', onPop)
    window.addEventListener('springreact:navigate', onNavigate)
    document.addEventListener('click', onClick)
    return () => {
      window.removeEventListener('popstate', onPop)
      window.removeEventListener('springreact:navigate', onNavigate)
      document.removeEventListener('click', onClick)
    }
  }, [])

  const route = resolveRoute(path, routes(), (window as any).__VIEW__)
  const page: ReactNode = createElement(ServerView, { name: route.view, key: route.view })
  return route.layout
    ? createElement(ServerView, { name: route.layout, slot: page })
    : page
}

/** Programmatic navigation (also exposed as window.SpringReact.navigate). */
export function navigate(path: string) {
  if (path !== location.pathname) history.pushState({}, '', path)
  window.dispatchEvent(new CustomEvent('springreact:navigate', { detail: path }))
}

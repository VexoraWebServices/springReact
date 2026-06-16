// Pure routing helpers (no DOM) — unit-testable on their own.

export type RouteInfo = { view: string; layout?: string; title?: string }
export type Routes = Record<string, RouteInfo> // keys are patterns, e.g. "/users/{id}"
export type Match = RouteInfo & { params: Record<string, string> }

/** Match a path against a pattern ("/users/{id}"); returns params or null if no match. */
export function matchPath(pattern: string, path: string): Record<string, string> | null {
  const pp = pattern.split('/').filter(Boolean)
  const ap = path.split('/').filter(Boolean)
  if (pp.length !== ap.length) return null
  const params: Record<string, string> = {}
  for (let i = 0; i < pp.length; i++) {
    const seg = pp[i]
    if (seg.startsWith('{') && seg.endsWith('}')) {
      params[seg.slice(1, -1)] = decodeURIComponent(ap[i])
    } else if (seg !== ap[i]) {
      return null
    }
  }
  return params
}

/**
 * Resolve a path to its route, extracting any dynamic params. Unknown paths fall back to
 * the server-provided initial view with no params.
 */
export function resolveRoute(path: string, routes: Routes, fallbackView: string): Match {
  for (const [pattern, info] of Object.entries(routes)) {
    const params = matchPath(pattern, path)
    if (params) return { ...info, params }
  }
  return { view: fallbackView, params: {} }
}

/** True when a path matches one of the app's route patterns (so we intercept the link). */
export function isInternalRoute(path: string, routes: Routes): boolean {
  return Object.keys(routes).some((pattern) => matchPath(pattern, path) !== null)
}

export type Layouts = Record<string, string> // layout name -> parent layout name

/**
 * The layout chain from innermost to outermost, e.g. layout "Admin" whose parent is "Root"
 * → ["Admin", "Root"]. Page is wrapped Admin(slot=Page), then Root(slot=Admin(...)).
 */
export function layoutChain(layout: string | undefined, layouts: Layouts): string[] {
  const chain: string[] = []
  const seen = new Set<string>()
  let l = layout
  while (l && !seen.has(l)) {
    chain.push(l)
    seen.add(l)
    l = layouts[l]
  }
  return chain
}

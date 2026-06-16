// Pure routing helpers (no DOM) — unit-testable on their own.

export type RouteInfo = { view: string; layout?: string }
export type Routes = Record<string, RouteInfo>

/**
 * Resolve a path to its route. Unknown paths fall back to the server-provided initial
 * view with no layout (so controller-rendered pages still work).
 */
export function resolveRoute(path: string, routes: Routes, fallbackView: string): RouteInfo {
  return routes[path] ?? { view: fallbackView }
}

/** True when a path has a client-navigable route (so we can intercept the link). */
export function isInternalRoute(path: string, routes: Routes): boolean {
  return path in routes
}

import { describe, expect, it } from 'vitest'
import { isInternalRoute, resolveRoute, type Routes } from '../src/routing'

const routes: Routes = {
  '/': { view: 'Home', layout: 'Main' },
  '/users': { view: 'Users', layout: 'Main' },
}

describe('resolveRoute', () => {
  it('resolves a known path to its view + layout', () => {
    expect(resolveRoute('/users', routes, 'Fallback')).toEqual({ view: 'Users', layout: 'Main' })
  })

  it('falls back to the initial view for unknown paths', () => {
    expect(resolveRoute('/nope', routes, 'Fallback')).toEqual({ view: 'Fallback' })
  })
})

describe('isInternalRoute', () => {
  it('detects app routes vs external links', () => {
    expect(isInternalRoute('/users', routes)).toBe(true)
    expect(isInternalRoute('/external', routes)).toBe(false)
  })
})

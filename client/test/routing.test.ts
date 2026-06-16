import { describe, expect, it } from 'vitest'
import { isInternalRoute, matchPath, resolveRoute, type Routes } from '../src/routing'

const routes: Routes = {
  '/': { view: 'Home', layout: 'Main' },
  '/users': { view: 'Users', layout: 'Main' },
  '/users/{id}': { view: 'User', layout: 'Main', title: 'User' },
}

describe('resolveRoute', () => {
  it('resolves a known path to its view + layout', () => {
    expect(resolveRoute('/users', routes, 'Fallback')).toEqual({
      view: 'Users',
      layout: 'Main',
      params: {},
    })
  })

  it('falls back to the initial view for unknown paths', () => {
    expect(resolveRoute('/nope', routes, 'Fallback')).toEqual({ view: 'Fallback', params: {} })
  })
})

describe('dynamic routes', () => {
  it('matchPath extracts params', () => {
    expect(matchPath('/users/{id}', '/users/42')).toEqual({ id: '42' })
    expect(matchPath('/users/{id}', '/users')).toBeNull()
    expect(matchPath('/users', '/users')).toEqual({})
  })

  it('resolveRoute returns the matched view + params', () => {
    expect(resolveRoute('/users/7', routes, 'X')).toEqual({
      view: 'User',
      layout: 'Main',
      title: 'User',
      params: { id: '7' },
    })
  })

  it('static routes still resolve with empty params', () => {
    expect(resolveRoute('/users', routes, 'X')).toEqual({
      view: 'Users',
      layout: 'Main',
      params: {},
    })
  })
})

describe('isInternalRoute', () => {
  it('detects app routes (incl. dynamic) vs external links', () => {
    expect(isInternalRoute('/users', routes)).toBe(true)
    expect(isInternalRoute('/users/99', routes)).toBe(true)
    expect(isInternalRoute('/external', routes)).toBe(false)
  })
})

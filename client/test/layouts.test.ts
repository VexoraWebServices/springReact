import { describe, expect, it } from 'vitest'
import { layoutChain, type Layouts } from '../src/routing'

describe('layoutChain (nested layouts)', () => {
  const layouts: Layouts = { Section: 'Root', Admin: 'Root' }

  it('chains a layout up to its parent (innermost → outermost)', () => {
    expect(layoutChain('Section', layouts)).toEqual(['Section', 'Root'])
  })

  it('returns a single layout when it has no parent', () => {
    expect(layoutChain('Root', layouts)).toEqual(['Root'])
  })

  it('returns nothing when there is no layout', () => {
    expect(layoutChain(undefined, layouts)).toEqual([])
  })

  it('is cycle-safe', () => {
    expect(layoutChain('A', { A: 'B', B: 'A' })).toEqual(['A', 'B'])
  })
})

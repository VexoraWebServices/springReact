import { describe, expect, it } from 'vitest'
import { applyOps, type Op, type UiNode } from '../src/live'

// A small server tree, mirroring what a Java Server Component emits.
const base: UiNode = {
  tag: 'div',
  props: { className: 'card' },
  children: [
    { tag: 'span', children: [{ text: 'Count: 0' }] },
    { tag: 'button', props: { onClick: { $action: 'inc' } }, children: [{ text: '+' }] },
  ],
}

const child = (n: any, i: number) => n.children[i]

describe('applyOps (client patch application)', () => {
  it('applies a text patch and never mutates the input tree', () => {
    const next = applyOps(base, [{ op: 'text', path: [0, 0], value: 'Count: 1' }])
    expect(child(child(next, 0), 0).text).toBe('Count: 1')
    // original is untouched (pure)
    expect(child(child(base, 0), 0).text).toBe('Count: 0')
  })

  it('sets and removes props', () => {
    const set = applyOps(base, [{ op: 'props', path: [0], set: { title: 'hi' } }])
    expect(child(set, 0).props.title).toBe('hi')

    const removed = applyOps(base, [{ op: 'props', path: [1], remove: ['onClick'] }])
    expect(child(removed, 1).props.onClick).toBeUndefined()
  })

  it('inserts and removes children', () => {
    const inserted = applyOps(base, [
      { op: 'insert', path: [], index: 2, node: { tag: 'p', children: [{ text: 'new' }] } },
    ])
    expect((inserted as any).children).toHaveLength(3)
    expect(child(inserted, 2).tag).toBe('p')

    const removed = applyOps(base, [{ op: 'remove', path: [], index: 1 }])
    expect((removed as any).children).toHaveLength(1)
    expect(child(removed, 0).tag).toBe('span')
  })

  it('replaces a node, including the root', () => {
    const swapped = applyOps(base, [
      { op: 'replace', path: [0], node: { tag: 'h1', children: [{ text: 'hi' }] } },
    ])
    expect(child(swapped, 0).tag).toBe('h1')

    const newRoot = applyOps(base, [{ op: 'replace', path: [], node: { text: 'gone' } }])
    expect(newRoot).toEqual({ text: 'gone' })
  })

  it('applies a batch of ops in order', () => {
    const ops: Op[] = [
      { op: 'text', path: [0, 0], value: 'Count: 2' },
      { op: 'insert', path: [], index: 2, node: { text: 'tail' } },
    ]
    const next = applyOps(base, ops)
    expect(child(child(next, 0), 0).text).toBe('Count: 2')
    expect((next as any).children[2].text).toBe('tail')
  })
})

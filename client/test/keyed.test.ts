import { describe, expect, it } from 'vitest'
import { applyOps, type Op, type UiNode } from '../src/live'

// A keyed list: <ul> with three keyed <li>.
const list: UiNode = {
  tag: 'ul',
  children: [
    { tag: 'li', props: { key: 1 }, children: [{ text: 'Item 1' }] },
    { tag: 'li', props: { key: 2 }, children: [{ text: 'Item 2' }] },
    { tag: 'li', props: { key: 3 }, children: [{ text: 'Item 3' }] },
  ],
}

const keysOf = (n: any) => n.children.map((c: any) => c.props.key)

describe('applyOps keyed reconciliation', () => {
  it('reorders existing children by key without re-creating them', () => {
    const op: Op = { op: 'keyed', path: [], items: [{ key: 3 }, { key: 2 }, { key: 1 }] }
    const next = applyOps(list, [op])
    expect(keysOf(next)).toEqual([3, 2, 1])
    // node identity preserved (text came along, not re-sent)
    expect((next as any).children[0].children[0].text).toBe('Item 3')
  })

  it('removes a child by omitting its key', () => {
    const op: Op = { op: 'keyed', path: [], items: [{ key: 2 }, { key: 3 }] }
    const next = applyOps(list, [op])
    expect(keysOf(next)).toEqual([2, 3])
  })

  it('inserts a new child carrying its node', () => {
    const op: Op = {
      op: 'keyed',
      path: [],
      items: [{ key: 1 }, { key: 4, node: { tag: 'li', props: { key: 4 }, children: [{ text: 'Item 4' }] } }],
    }
    const next = applyOps(list, [op])
    expect(keysOf(next)).toEqual([1, 4])
    expect((next as any).children[1].children[0].text).toBe('Item 4')
  })
})

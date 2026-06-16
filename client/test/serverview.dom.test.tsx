// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { act } from 'react'
import { createRoot, type Root } from 'react-dom/client'
import { fireEvent } from '@testing-library/dom'

// ---- Mock WebSocket: capture sends, let the test push server messages -------------
class MockWS {
  static OPEN = 1
  static instances: MockWS[] = []
  url: string
  readyState = 1 // OPEN immediately
  sent: any[] = []
  onopen: (() => void) | null = null
  onmessage: ((e: { data: string }) => void) | null = null
  onclose: (() => void) | null = null
  onerror: (() => void) | null = null
  constructor(url: string) {
    this.url = url
    MockWS.instances.push(this)
    queueMicrotask(() => this.onopen?.())
  }
  send(s: string) { this.sent.push(JSON.parse(s)) }
  close() { this.onclose?.() }
  deliver(msg: any) { this.onmessage?.({ data: JSON.stringify(msg) }) }
  static last() { return MockWS.instances[MockWS.instances.length - 1] }
  static reset() { MockWS.instances = [] }
}

let container: HTMLDivElement
let root: Root

beforeEach(() => {
  vi.resetModules() // fresh live.ts singletons (socket/connecting) per test
  ;(globalThis as any).WebSocket = MockWS as any
  ;(globalThis as any).location = { protocol: 'http:', host: 'localhost' }
  MockWS.reset()
  container = document.createElement('div')
  document.body.appendChild(container)
})

afterEach(() => {
  act(() => root?.unmount())
  container.remove()
})

// Import a fresh ServerView (with fresh module state), mount it, flush connect.
async function mount(name: string, extra: Record<string, unknown> = {}) {
  const { default: ServerView } = await import('../src/ServerView')
  await act(async () => {
    root = createRoot(container)
    root.render(<ServerView name={name} {...extra} />)
  })
  await act(async () => { await Promise.resolve() })
}

function ws() {
  const w = MockWS.last()
  if (!w) throw new Error('no websocket opened')
  return w
}
const idOf = () => ws().sent[0].id

describe('ServerView in the browser (jsdom)', () => {
  const counterTree = (n: number) => ({
    tag: 'div',
    children: [
      { tag: 'span', props: { className: 'count' }, children: [{ text: `Count: ${n}` }] },
      { tag: 'button', props: { onClick: { $action: 'inc' } }, children: [{ text: '+' }] },
    ],
  })

  it('mounts the component and renders the server tree', async () => {
    await mount('Counter')
    expect(ws().sent.find((m) => m.t === 'mount' && m.c === 'Counter')).toBeTruthy()
    await act(async () => ws().deliver({ t: 'tree', id: idOf(), tree: counterTree(0) }))
    expect(container.textContent).toContain('Count: 0')
  })

  it('button click sends the action and the patch updates the DOM', async () => {
    await mount('Counter')
    const id = idOf()
    await act(async () => ws().deliver({ t: 'tree', id, tree: counterTree(0) }))

    await act(async () => fireEvent.click(container.querySelector('button')!))
    expect(ws().sent.find((m) => m.t === 'call' && m.action === 'inc')).toBeTruthy()

    await act(async () =>
      ws().deliver({ t: 'patch', id, ops: [{ op: 'text', path: [0, 0], value: 'Count: 1' }] }),
    )
    expect(container.textContent).toContain('Count: 1')
  })

  it('keyed list add via patch renders the new item', async () => {
    await mount('List')
    const id = idOf()
    const list = (keys: number[]) => ({
      tag: 'ul',
      children: keys.map((k) => ({ tag: 'li', props: { key: k }, children: [{ text: `Item ${k}` }] })),
    })
    await act(async () => ws().deliver({ t: 'tree', id, tree: list([1]) }))
    expect(container.textContent).toContain('Item 1')

    await act(async () =>
      ws().deliver({
        t: 'patch',
        id,
        ops: [{ op: 'keyed', path: [], items: [{ key: 1 }, { key: 2, node: { tag: 'li', props: { key: 2 }, children: [{ text: 'Item 2' }] } }] }],
      }),
    )
    expect(container.textContent).toContain('Item 2')
  })

  it('checkbox onChange sends the action with its args', async () => {
    await mount('Check')
    const id = idOf()
    await act(async () =>
      ws().deliver({
        t: 'tree',
        id,
        tree: { tag: 'input', props: { type: 'checkbox', checked: false, onChange: { $action: 'toggle', args: [7] } } },
      }),
    )
    await act(async () => fireEvent.click(container.querySelector('input')!))
    expect(ws().sent.find((m) => m.t === 'call' && m.action === 'toggle')?.args).toEqual([7])
  })

  it('form submit collects named fields and sends them', async () => {
    await mount('Form')
    const id = idOf()
    await act(async () =>
      ws().deliver({
        t: 'tree',
        id,
        tree: {
          tag: 'form',
          props: { onSubmit: { $action: 'add', form: true } },
          children: [
            { tag: 'input', props: { type: 'text', name: 'text' } },
            { tag: 'button', props: { type: 'submit' }, children: [{ text: 'Add' }] },
          ],
        },
      }),
    )
    const input = container.querySelector('input')!
    await act(async () => fireEvent.change(input, { target: { value: 'Buy milk' } }))
    await act(async () => fireEvent.submit(container.querySelector('form')!))
    expect(ws().sent.find((m) => m.t === 'call' && m.action === 'add')?.args?.[0]).toEqual({ text: 'Buy milk' })
  })
})

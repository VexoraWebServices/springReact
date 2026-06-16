// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { act } from 'react'
import { createRoot, type Root } from 'react-dom/client'
import { fireEvent } from '@testing-library/dom'

class MockWS {
  static OPEN = 1
  static instances: MockWS[] = []
  url: string
  readyState = 1
  sent: any[] = []
  onopen: (() => void) | null = null
  onmessage: ((e: { data: string }) => void) | null = null
  onclose: (() => void) | null = null
  constructor(url: string) {
    this.url = url
    MockWS.instances.push(this)
    queueMicrotask(() => this.onopen?.())
  }
  send(s: string) { this.sent.push(JSON.parse(s)) }
  close() { this.onclose?.() }
  deliver(msg: any) { this.onmessage?.({ data: JSON.stringify(msg) }) }
  static reset() { MockWS.instances = [] }
  // all sends across every socket on the page (one shared socket in reality)
  static allSent() { return MockWS.instances.flatMap((w) => w.sent) }
}

let container: HTMLDivElement
let root: Root

// Mirror the real shell that ReactRenderer emits.
function setupShell() {
  ;(globalThis as any).WebSocket = MockWS as any
  ;(window as any).__VIEW__ = 'Todos'
  ;(window as any).__ROUTES__ = { '/': { view: 'Todos', layout: 'Main', title: 'Todos' } }
  ;(window as any).__LAYOUTS__ = {}
  ;(window as any).__SSR__ = {
    Main: { tag: 'div', props: { className: 'app' }, children: [{ tag: 'main', children: [{ tag: '$slot' }] }] },
    Todos: {
      tag: 'div',
      props: { className: 'card' },
      children: [
        { tag: 'h1', children: [{ text: 'Todos' }] },
        { tag: 'button', props: { className: 'primary', onClick: { $action: 'addOne' } }, children: [{ text: 'Add one' }] },
        { tag: 'ul', props: { className: 'list' }, children: [] },
      ],
    },
  }
  window.history.pushState({}, '', '/')
}

beforeEach(() => {
  vi.resetModules()
  MockWS.reset()
  setupShell()
  container = document.createElement('div')
  document.body.appendChild(container)
})
afterEach(() => {
  act(() => root?.unmount())
  container.remove()
})

describe('Real boot: Router + SSR seed + layout', () => {
  it('SSR-seeds content, mounts layout+page, click sends call, patch updates DOM', async () => {
    const { default: Router } = await import('../src/Router')

    await act(async () => {
      root = createRoot(container)
      root.render(<Router />)
    })
    await act(async () => { await Promise.resolve() })

    // 1) SSR seed: content visible before any WS tree message.
    expect(container.textContent).toContain('Todos')
    expect(container.querySelector('.app')).toBeTruthy() // layout rendered
    expect(container.querySelector('.card')).toBeTruthy() // page rendered into slot

    // 2) Both layout and page mounted over the (shared) socket.
    const mounts = MockWS.allSent().filter((m) => m.t === 'mount').map((m) => m.c)
    expect(mounts).toContain('Main')
    expect(mounts).toContain('Todos')

    // 3) Server confirms the page tree (same as SSR).
    const todosMount = MockWS.allSent().find((m) => m.t === 'mount' && m.c === 'Todos')
    await act(async () =>
      MockWS.instances.forEach((w) =>
        w.deliver({ t: 'tree', id: todosMount.id, tree: (window as any).__SSR__.Todos }),
      ),
    )

    // 4) Click the button → a call must be sent.
    await act(async () => fireEvent.click(container.querySelector('.primary')!))
    const call = MockWS.allSent().find((m) => m.t === 'call' && m.action === 'addOne')
    expect(call).toBeTruthy()

    // 5) Server patch adds a list item → DOM updates.
    await act(async () =>
      MockWS.instances.forEach((w) =>
        w.deliver({
          t: 'patch',
          id: todosMount.id,
          ops: [{ op: 'insert', path: [2], index: 0, node: { tag: 'li', props: { key: 1 }, children: [{ text: 'Item 1' }] } }],
        }),
      ),
    )
    expect(container.textContent).toContain('Item 1')
  })
})

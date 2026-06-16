import { useCallback, useEffect, useRef, useState } from 'react'

// One WebSocket for the whole app. Every live component multiplexes over it.
export type LiveMessage = { t: string; id: string; [k: string]: unknown }
type MessageListener = (msg: LiveMessage) => void

let socket: WebSocket | null = null
let connecting: Promise<void> | null = null
let counter = 0
const listeners = new Map<string, MessageListener>()
const outbox: string[] = []

function liveUrl(): string {
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  return `${proto}://${location.host}/live`
}

function connect(): Promise<void> {
  if (connecting) return connecting
  connecting = new Promise<void>((resolve) => {
    const ws = new WebSocket(liveUrl())
    socket = ws
    ws.onopen = () => {
      outbox.splice(0).forEach((m) => ws.send(m))
      resolve()
    }
    ws.onmessage = (e) => {
      const msg = JSON.parse(e.data) as LiveMessage
      if (msg.t === 'error') console.error('[springreact]', msg.id, (msg as any).message)
      else listeners.get(msg.id)?.(msg)
    }
    ws.onclose = () => {
      socket = null
      connecting = null
    }
  })
  return connecting
}

function send(message: unknown) {
  const json = JSON.stringify(message)
  if (socket && socket.readyState === WebSocket.OPEN) socket.send(json)
  else {
    outbox.push(json)
    connect()
  }
}

export type Call = (action: string, ...args: unknown[]) => void

// Shared mount/subscribe/unmount lifecycle for any live component on the page.
function useLiveChannel(component: string, onMessage: MessageListener): Call {
  const idRef = useRef<string>('')
  if (!idRef.current) idRef.current = `${component}-${++counter}`
  const id = idRef.current
  const handler = useRef(onMessage)
  handler.current = onMessage

  useEffect(() => {
    listeners.set(id, (msg) => handler.current(msg))
    connect().then(() => send({ t: 'mount', id, c: component }))
    return () => {
      send({ t: 'unmount', id })
      listeners.delete(id)
    }
  }, [id, component])

  return useCallback((action, ...args) => send({ t: 'call', id, action, args }), [id])
}

/** A serialized UI node from a Java Server Component. */
export type UiNode =
  | { text: string }
  | { tag: string; props?: Record<string, any>; children?: UiNode[] }

/** A patch op produced by the server's UiTreeDiff. */
export type Op = {
  op: 'text' | 'props' | 'insert' | 'remove' | 'replace'
  path: number[]
  value?: string
  set?: Record<string, unknown>
  remove?: string[]
  index?: number
  node?: UiNode
}

function childrenOf(node: any): UiNode[] {
  if (!node.children) node.children = []
  return node.children
}

function nodeAt(root: UiNode, path: number[]): any {
  let n: any = root
  for (const i of path) n = childrenOf(n)[i]
  return n
}

/** Apply a batch of patch ops to a tree, returning a new tree (input untouched). */
export function applyOps(root: UiNode, ops: Op[]): UiNode {
  let tree: UiNode = structuredClone(root)
  for (const op of ops) {
    if (op.op === 'replace') {
      if (op.path.length === 0) {
        tree = op.node as UiNode
      } else {
        const parent = nodeAt(tree, op.path.slice(0, -1))
        childrenOf(parent)[op.path[op.path.length - 1]] = op.node as UiNode
      }
    } else if (op.op === 'text') {
      ;(nodeAt(tree, op.path) as any).text = op.value
    } else if (op.op === 'props') {
      const n = nodeAt(tree, op.path)
      n.props = { ...(n.props ?? {}) }
      if (op.set) Object.assign(n.props, op.set)
      if (op.remove) for (const k of op.remove) delete n.props[k]
    } else if (op.op === 'insert') {
      childrenOf(nodeAt(tree, op.path)).splice(op.index!, 0, op.node as UiNode)
    } else if (op.op === 'remove') {
      childrenOf(nodeAt(tree, op.path)).splice(op.index!, 1)
    }
  }
  return tree
}

/**
 * Bind a React component to a Java Server Component. Returns the rendered UI tree (null
 * until the first render arrives) and a `call` to invoke server actions. Handles both the
 * initial full `tree` and incremental `patch` messages.
 */
export function useServerComponent(component: string) {
  const shadow = useRef<UiNode | null>(null)
  const [tree, setTree] = useState<UiNode | null>(null)

  const call = useLiveChannel(component, (msg) => {
    if (msg.t === 'tree') {
      shadow.current = msg.tree as UiNode
      setTree(shadow.current)
    } else if (msg.t === 'patch' && shadow.current) {
      const next = applyOps(shadow.current, msg.ops as Op[])
      shadow.current = next
      setTree(next)
    }
  })

  return { tree, call }
}

/**
 * Bind to a live component that exposes raw `@LiveState` (for clients that render with
 * their own TSX rather than a server tree).
 */
export function useLive<S = Record<string, unknown>>(component: string, initial: Partial<S> = {}) {
  const [state, setState] = useState<S>(initial as S)
  const call = useLiveChannel(component, (msg) => {
    if (msg.t === 'state') setState(msg.state as S)
  })
  return { state, call }
}

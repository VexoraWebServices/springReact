import { createElement, Fragment, type ReactNode } from 'react'
import { useServerComponent, type Call, type UiNode } from './live'
import { widgetRegistry } from './widgets'

// Gather a form's named fields into a plain object (checkboxes → boolean).
function collectForm(e: any): Record<string, unknown> {
  const form: HTMLFormElement | null = e?.currentTarget ?? e?.target
  const data: Record<string, unknown> = {}
  const elements = form?.elements as any
  if (elements) {
    for (const el of Array.from(elements) as any[]) {
      if (!el?.name) continue
      data[el.name] = el.type === 'checkbox' ? el.checked : el.value
    }
  }
  return data
}

// Turn a server-sent UI node into real React elements.
// - event props { onClick: { $action, args, event } } become handlers that call the server
// - tag "$widget" renders a registered custom React component
// - tag "$slot" renders the child screen a layout wraps
function render(node: UiNode | null, call: Call, slot: ReactNode, key?: string | number): ReactNode {
  if (node == null) return null
  if ('text' in node) return node.text
  if (node.tag === '$slot') return slot ?? null

  const rawProps = node.props ?? {}

  if (node.tag === '$widget') {
    const { name, ...rest } = rawProps
    const Widget = widgetRegistry[name]
    if (!Widget) return createElement('span', { key }, `[unknown widget: ${name}]`)
    const kids = (node.children ?? []).map((c, i) => render(c, call, slot, i))
    return createElement(Widget, { key, ...rest, call }, ...kids)
  }

  const reactKey = (rawProps as any).key ?? key
  const props: Record<string, unknown> = {}
  for (const [name, value] of Object.entries(rawProps)) {
    if (name === 'key') continue // reserved by React; applied separately below
    if (name.startsWith('on') && value && typeof value === 'object' && '$action' in value) {
      const binding = value as {
        $action: string
        args?: unknown[]
        event?: string
        form?: boolean
      }
      props[name] = (e: any) => {
        if (name === 'onSubmit') e?.preventDefault?.()
        const args: unknown[] = []
        if (binding.form) args.push(collectForm(e))
        if (binding.event) args.push(e?.target?.[binding.event])
        if (binding.args) args.push(...binding.args)
        call(binding.$action, ...args)
      }
    } else {
      props[name] = value
    }
  }

  const children = (node.children ?? []).map((child, i) => render(child, call, slot, i))
  return createElement(node.tag, { key: reactKey, ...props }, ...children)
}

/**
 * The universal client runtime: mounts a server component by name and renders whatever
 * tree the server sends, applying patches as they arrive. `slot` is the child screen a
 * layout renders into. This is the only component your app screens need.
 */
export default function ServerView({
  name,
  slot,
  params,
}: {
  name: string
  slot?: ReactNode
  params?: Record<string, unknown>
}) {
  const { tree, call } = useServerComponent(name, params)
  return createElement(Fragment, null, render(tree, call, slot, 'root'))
}

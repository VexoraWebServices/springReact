import { createElement, Fragment, type ReactNode } from 'react'
import { useServerComponent, type Call, type UiNode } from './live'
import { widgetRegistry } from './widgets'

// Turn a server-sent UI node into real React elements. Event props like
// { onClick: { $action, args, event } } become handlers that call the server.
// A node with tag "$widget" renders a registered custom React component.
function render(node: UiNode | null, call: Call, key?: string | number): ReactNode {
  if (node == null) return null
  if ('text' in node) return node.text

  const rawProps = node.props ?? {}

  if (node.tag === '$widget') {
    const { name, ...rest } = rawProps
    const Widget = widgetRegistry[name]
    if (!Widget) return createElement('span', { key }, `[unknown widget: ${name}]`)
    const kids = (node.children ?? []).map((c, i) => render(c, call, i))
    return createElement(Widget, { key, ...rest, call }, ...kids)
  }

  const props: Record<string, unknown> = { key }
  for (const [name, value] of Object.entries(rawProps)) {
    if (name.startsWith('on') && value && typeof value === 'object' && '$action' in value) {
      const binding = value as { $action: string; args?: unknown[]; event?: string }
      props[name] = (e: any) => {
        const args: unknown[] = []
        if (binding.event) args.push(e?.target?.[binding.event])
        if (binding.args) args.push(...binding.args)
        call(binding.$action, ...args)
      }
    } else {
      props[name] = value
    }
  }

  const children = (node.children ?? []).map((child, i) => render(child, call, i))
  return createElement(node.tag, props, ...children)
}

/**
 * The universal client runtime: mounts a Java Server Component by name and renders
 * whatever tree the server sends, applying patches as they arrive. This is the only
 * component your app screens need — the screens themselves live in Java/Kotlin.
 */
export default function ServerView({ name }: { name: string }) {
  const { tree, call } = useServerComponent(name)
  return createElement(Fragment, null, render(tree, call, 'root'))
}

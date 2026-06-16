import type { ComponentType } from 'react'
import type { Call } from './live'

/**
 * Props every custom widget receives: whatever the Java side passed via `attr(...)`,
 * plus `call` to invoke server actions.
 */
export type WidgetProps = Record<string, any> & { call: Call }

export const widgetRegistry: Record<string, ComponentType<any>> = {}

/** Register a custom React component so Java screens can embed it via `widget("Name", ...)`. */
export function registerWidget(name: string, component: ComponentType<any>): void {
  widgetRegistry[name] = component
}

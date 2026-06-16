package io.springreact.live

/**
 * Optional lifecycle callbacks for a component. Implement alongside `ServerComponent` to
 * run code when an instance mounts (a tab opened it) and unmounts (navigated away, tab
 * closed, or disconnected) — ideal for presence, subscriptions, and cleanup.
 *
 * ```
 * @LiveComponent("Room")
 * class RoomScreen(private val presence: Presence) : ServerComponent, LiveLifecycle {
 *     override fun onMount()   { presence.join(LiveContext.current().principalName()) }
 *     override fun onUnmount() { presence.leave(...) }
 *     override fun render() = ...
 * }
 * ```
 */
interface LiveLifecycle {
    fun onMount() {}
    fun onUnmount() {}
}

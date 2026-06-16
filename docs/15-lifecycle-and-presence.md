# 15. Lifecycle Hooks & Presence

A component can run code when it **mounts** (a tab opened it) and **unmounts** (navigated
away, tab closed, or disconnected). Implement `LiveLifecycle` alongside `ServerComponent`.

```kotlin
@LiveComponent("Room")
class RoomScreen(private val presence: Presence) : ServerComponent, LiveLifecycle {

    override fun onMount() {
        presence.join(LiveContext.current().principalName() ?: "guest")
    }

    override fun onUnmount() {
        presence.leave(LiveContext.current().principalName() ?: "guest")
    }

    override fun render(): UiNode =
        div(h2("In the room:"), ul(presence.list().map { li(it) }))
}
```

- `onMount` runs after the instance is created (params already applied), before the first
  render. `LiveContext.current()` is available inside it.
- `onUnmount` runs on explicit unmount **and** when the connection drops — so cleanup is
  reliable.

## Presence in one screen

Combine lifecycle hooks with [broadcast](06-realtime-broadcast.md) for a live "who's
online" list:

```kotlin
@Service
class Presence(private val live: LiveBroadcaster) {
    private val online = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
    fun join(u: String) { online.add(u); live.broadcast("Room") }
    fun leave(u: String) { online.remove(u); live.broadcast("Room") }
    fun list() = online.toList()
}
```

Now opening or closing a tab updates the list for everyone, automatically.

## TODO checklist

- [ ] Implement `LiveLifecycle` on a component
- [ ] Register/unregister something in `onMount`/`onUnmount`
- [ ] Broadcast on change for a live presence list
- [ ] Next: [How It Works](12-how-it-works.md)

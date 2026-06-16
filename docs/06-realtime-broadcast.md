# 6. Realtime Broadcast

By default each user has their own state. For **shared, live** UI — dashboards, presence,
chat, "X people online", live scores — use `LiveBroadcaster` to push a re-render to
**every** connected client.

## The pattern

1. Keep the shared data in a normal Spring `@Service`.
2. In an action, change the data, then call `broadcast("ComponentName")`.
3. Every mounted instance of that component re-renders and the change reaches all browsers.

```kotlin
@Service
class ScoreBoard {
    private val score = AtomicInteger(0)
    fun get() = score.get()
    fun add(points: Int) = score.addAndGet(points)
}

@LiveComponent("Scoreboard")
class ScoreboardScreen(
    private val board: ScoreBoard,
    private val live: LiveBroadcaster,           // inject the broadcaster
) : ServerComponent {

    @LiveAction
    fun goal() {
        board.add(1)
        live.broadcast("Scoreboard")             // everyone updates
    }

    override fun render(): UiNode =
        div(cls("card"),
            h1("Score: ${board.get()}"),
            button(onClick("goal"), "Goal!"))
}
```

Open the page in two browsers. Click "Goal!" in one — **both** update instantly.

## What `broadcast` does

```
live.broadcast("Scoreboard")
        │
For every mounted "Scoreboard" across all sessions:
   re-render it  →  diff against what that client last saw  →  send a patch
```

Each client gets a minimal patch reflecting the new shared state. You don't track sessions
yourself.

## Presence example (who's online)

```kotlin
@Service
class Presence {
    private val online = ConcurrentHashMap.newKeySet<String>()
    fun join(user: String) { online.add(user) }
    fun leave(user: String) { online.remove(user) }
    fun list() = online.toList()
}
```

Call `presence.join(...)` then `live.broadcast("OnlineList")` whenever the set changes, and
every `OnlineList` screen refreshes.

## Tips

- Broadcast is keyed by **component name**, so only that component's clients re-render.
- Call it from anywhere you can inject `LiveBroadcaster` — an action, a `@Service`, a
  `@Scheduled` job pushing periodic updates, a message listener, etc.
- It's safe to call from background threads.

## TODO checklist

- [ ] Put shared state in a `@Service`
- [ ] Inject `LiveBroadcaster` into a component
- [ ] In an action: mutate the service, then `broadcast("Name")`
- [ ] Open two tabs and watch both update
- [ ] Next: [Custom Widgets](07-custom-widgets.md)

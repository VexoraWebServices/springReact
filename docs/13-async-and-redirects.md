# 13. Async, Loading States & Redirects

Sometimes an action can't finish immediately (it calls a slow API) or should send the user
somewhere else. Both use `LiveContext`, available **inside an action** via
`LiveContext.current()`.

## Loading states & async work

The trick: show a loading state right away, do the slow work in the background, then push
an update when it's done. Keep a `handle()` and call `update()` later.

```kotlin
@LiveComponent("Report")
class ReportScreen(private val api: ReportApi) : ServerComponent {

    @LiveState var status = "idle"          // "idle" | "loading" | "done"
    @LiveState var rows = listOf<String>()

    @LiveAction
    fun load() {
        val handle = LiveContext.current().handle()   // grab a handle NOW (inside the action)
        status = "loading"                             // this renders immediately
        Thread {                                       // or a coroutine / @Async / executor
            rows = api.fetchSlowly()
            status = "done"
            handle.update()                            // re-render this screen, push the diff
        }.start()
    }

    override fun render(): UiNode = when (status) {
        "loading" -> p("Loading…")
        "done"    -> ul(rows.map { li(it) })
        else      -> button(onClick("load"), "Load report")
    }
}
```

What the user sees: click → "Loading…" instantly → the list appears when the data arrives.
No polling, no manual WebSocket code.

> Important: call `LiveContext.current().handle()` **synchronously inside the action**
> (not from the background thread). The handle is safe to use from any thread afterwards.

## Redirects

Send the user to another route from the server:

```kotlin
@LiveAction
fun save(form: TodoForm, errors: LiveErrors) {
    if (errors.hasErrors()) { ... ; return }
    repo.save(form)
    LiveContext.current().redirect("/todos")   // client navigates, no reload
}
```

The client receives a navigate message and performs client-side navigation (same as
clicking a link).

## When to use what

| You want… | Use |
|---|---|
| Update *this* screen after async work | `LiveContext.current().handle().update()` |
| Send *this* user to another route | `LiveContext.current().redirect("/path")` |
| Update *everyone* (shared state) | `LiveBroadcaster.broadcast("Component")` — see [Realtime](06-realtime-broadcast.md) |

## TODO checklist

- [ ] An action that sets `status="loading"`, does work on a thread, then `handle.update()`
- [ ] Render different UI per status
- [ ] An action that `redirect(...)`s after success
- [ ] Next: [Configuration](10-configuration.md)

# 2. Server Components

A **server component** is a class that describes one piece of UI. It has three parts:

1. **State** — fields marked `@LiveState`
2. **Actions** — methods marked `@LiveAction`
3. **render()** — returns what the UI looks like right now

```kotlin
@LiveComponent("Counter")
class CounterScreen : ServerComponent {
    @LiveState var count = 0
    @LiveAction fun increment() { count++ }
    @LiveAction fun add(amount: Int) { count += amount }   // actions can take arguments
    override fun render(): UiNode =
        div(button(onClick("increment"), "+1"),
            button(onClick("add", 10), "+10"),
            span("Count: $count"))
}
```

## `@LiveComponent("Name")`

Registers the class as a component named `"Name"`. The name is how the browser mounts it
and how `@Route`/layouts/`useServerComponent` refer to it.

In Kotlin, mark state with `@LiveState` directly (Kotlin puts it on the backing field):

```kotlin
@LiveState var count = 0
```

## `@LiveState`

Only `@LiveState` fields are sent to the browser. Everything else (injected services,
private helpers) stays private to the server.

```kotlin
@LiveState var name = ""          // sent to the client
private val repo = ...            // NOT sent
```

Supported types: anything Jackson can serialize — numbers, strings, booleans, lists, maps,
and data classes.

## `@LiveAction`

A method the browser can invoke. Arguments are deserialized from the client call:

```kotlin
@LiveAction fun rename(newName: String) { name = newName }
@LiveAction fun toggle(id: Int) { ... }
```

The action name defaults to the method name; override it: `@LiveAction("save")`.

After **every** action, SpringReact re-renders the component and sends only what changed.
You never manually push updates for normal actions.

## Dependency Injection

A component is a real Spring bean — inject anything via the constructor:

```kotlin
@LiveComponent("Users")
class UsersScreen(private val users: UserRepository) : ServerComponent {
    override fun render(): UiNode =
        ul(users.findAll().map { li(it.name) })
}
```

A **fresh, fully-injected instance** is created for each mounted component, per browser
tab. State is therefore per-user-per-tab by default. (For *shared* state across users, see
[Realtime Broadcast](06-realtime-broadcast.md).)

## render()

Returns a `UiNode` built with the [HTML DSL](03-html-dsl.md). It's called on mount and
after each action. Keep it pure — read state, return UI; don't do side effects here.

## Gotchas

- **Kotlin name clash:** a `var name` generates a `setName(...)` method. Don't also write
  `@LiveAction fun setName(...)` — rename the property (e.g. `who`) or the action.
- render() runs often; do expensive work in actions, not in render.

## TODO checklist

- [ ] One `@LiveComponent`
- [ ] At least one `@LiveState` field and one `@LiveAction`
- [ ] Inject a service via the constructor
- [ ] Next: [The HTML DSL](03-html-dsl.md)

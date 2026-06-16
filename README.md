# SpringReact

A **Kotlin-first Spring Boot framework** for building React UIs without leaving the JVM.
You write screens as **Kotlin (or Java) server components**; the framework renders them to
a React element tree, streams it (and incremental patches) to a runtime it **bundles inside
its own jar**, and reconciles it with real React over **one WebSocket**.

Like Thymeleaf: add the dependency, write components, run. **No separate frontend project,
no npm, no REST glue.** One process, one port, one jar.

```kotlin
@LiveComponent("Home")
@Route("/", layout = "Main")
class HomeScreen(private val greetings: GreetingService) : ServerComponent {

    @LiveState var count = 0
    @LiveAction fun increment() { count++ }

    override fun render(): UiNode =
        div(cls("card"),
            h1(greetings.hello("world")),
            button(onClick("increment"), "Count: $count"))
}
```

## Features

- **Server components** — `@LiveComponent` + `ServerComponent.render()` in Kotlin or Java,
  with full Spring DI. State lives in the JVM (`@LiveState`); events are `@LiveAction`s.
- **One WebSocket transport** — no REST, no client store. Full tree on mount, minimal
  **diff patches** after that.
- **Routing & client navigation** — `@Route("/path", layout="Main")` declares a screen's
  URL (no controller needed). The bundled runtime does client-side navigation (no full
  reload) via the injected `window.__ROUTES__` table.
- **Layouts** — `Html.slot()`; a layout component stays mounted while the inner screen
  (keyed by view) swaps on navigation.
- **Realtime broadcast** — inject `LiveBroadcaster`; `broadcast("Component")` re-renders
  every mounted client (live dashboards, presence, chat).
- **Forms + validation** — `onSubmit` binds named fields to a typed Kotlin DTO; a
  `LiveErrors` parameter is filled from Bean Validation before your action runs.
- **Keyed reconciliation** — `key()` on list children → minimal `keyed` patches on
  reorder/insert/remove (no index churn).
- **Custom widgets** — `widget("Name", attr(...))` renders a registered client React
  component (charts, canvas, animations) while logic stays on the server.
- **Authorization** — `@LiveAuthorize("ADMIN")` guards actions; a pluggable `LiveSecurity`
  bean decides (bridge it to Spring Security).
- **Bundled runtime** — the React runtime is esbuild-bundled into the jar and served at
  `/springreact/springreact.js`. Consumers ship no frontend files.

## Documentation

Beginner-friendly guides with copy-paste examples live in **[`docs/`](docs/README.md)** —
one page per feature, plus a full [Todo app tutorial](docs/11-tutorial-todo-app.md) and a
[How It Works](docs/12-how-it-works.md) deep dive. Start at
[Getting Started](docs/01-getting-started.md).

## Module layout

```
SpringReact/                       (Kotlin, build.gradle.kts)
├── src/main/kotlin/io/springreact/
│   ├── jsc/        Html DSL, UiNode/Element/Text/Attr, ServerComponent, UiTreeDiff
│   ├── live/       @LiveComponent/@LiveState/@LiveAction, registry, WebSocket handler,
│   │               LiveBroadcaster, LiveErrors, auto-config
│   ├── web/        @Route + RouteRegistry, ReactView/ReactViewResolver
│   └── autoconfigure/  ReactProperties, ReactRenderer, ReactAutoConfiguration
├── src/test/kotlin/io/springreact/it/   integration tests (live, routing, broadcast,
│                                        forms, keyed) — drive the real /live socket
└── client/         the bundled runtime (esbuild): ServerView, Router, hooks, patch
                    application, widget registry  (+ vitest unit tests)
```

## Build & test — one command

```bash
./gradlew build
```

Compiles Kotlin, esbuild-bundles the runtime into the jar, and runs **both** suites:

- **9 Spring integration tests** over the real `/live` WebSocket — live engine (DI,
  widgets, diffing), shell + routing, broadcast (two clients), form validation, keyed
  reconciliation, and authorization.
- **11 client unit tests** (vitest) — patch application, route resolution, keyed ops —
  plus a TypeScript typecheck.

`npm install` and the client tests run automatically as Gradle tasks; no manual steps.

## Configuration (`spring.react.*`)

| Property                       | Default                       | Meaning                                  |
|--------------------------------|-------------------------------|------------------------------------------|
| `spring.react.title`           | `SpringReact`                 | default `<title>`                        |
| `spring.react.runtime-path`    | `/springreact/springreact.js` | URL of the bundled runtime               |
| `spring.react.allowed-origins` | `*`                           | `/live` WebSocket origin allowlist       |

## Versions

Kotlin 2.1.0 · Spring Boot 3.4.1 · Java 21 toolchain · Gradle Kotlin DSL ·
node-gradle 7.1.0 (system Node) · React 18 (bundled).

## Roadmap

Suspense-style streaming render · async self-updating components (loading states) · a
project starter/initializer · publishing to Maven Central (POM metadata is in place).

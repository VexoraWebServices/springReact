# SpringReact

A **Kotlin Spring Boot framework** for building React UIs without leaving the JVM. You
write screens as **Kotlin server components**; the framework renders them to a React
element tree, streams it (and incremental patches) to a runtime it **bundles inside its own
jar**, and reconciles it with real React over **one WebSocket**.

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

## Quick start (Gradle plugin)

```kotlin
plugins { id("com.vexora.springreact") version "0.1.0" }
```

That one line configures Kotlin, Spring Boot, dependency management, and the framework. See
[the Gradle plugin docs](docs/17-gradle-plugin.md) and [`examples/minimal`](examples/minimal).

## Features

- **Server components** — `@LiveComponent` + `ServerComponent.render()` in Kotlin, with
  full Spring DI. State lives in the JVM (`@LiveState`); events are `@LiveAction`s.
- **One WebSocket transport** — no REST, no client store. Full tree on mount, minimal
  **diff patches** after that.
- **Routing & client navigation** — `@Route("/path", layout="Main", title="…")` declares a
  screen's URL (no controller needed), including **dynamic params** (`/users/{id}` →
  `@LiveParam`) and per-route titles. The bundled runtime does client-side navigation (no
  full reload) via the injected `window.__ROUTES__` table.
- **Layouts (incl. nested)** — `Html.slot()`; layouts stay mounted while the inner screen
  swaps. `@Layout(parent="…")` nests layouts (root → section → page).
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
- **Async, loading & redirects** — `LiveContext.current()` gives an action a `handle()` to
  push a re-render after background work (loading states) and `redirect("/path")` for
  server-initiated navigation.
- **Not-found & error boundaries** — `spring.react.not-found-view` (404) and
  `spring.react.error-view` (render failures) render your own components.
- **Middleware** — `LiveInterceptor` beans run before every action (logging, tenancy, rate
  limiting, blocking).
- **Lifecycle hooks** — `LiveLifecycle.onMount/onUnmount` (reliable on disconnect) for
  presence, subscriptions, cleanup; plus `LiveContext` access to headers/cookies/principal.
- **i18n** — `LiveContext.locale()` (from Accept-Language) + Spring `MessageSource` render
  screens in the user's language.
- **Server-side rendering** — the initial screen + layouts are pre-rendered to HTML on
  the JVM (no Node), so View Source / crawlers / first paint show real content; the client
  seeds from it and takes over. Toggle with `spring.react.ssr`.
- **Styling & npm modules** — point `spring.react.stylesheets` at your CSS (Tailwind,
  etc.) and `spring.react.scripts` at a custom-widget bundle; the runtime exposes its
  React (`window.SpringReact.React`) so widget bundles (three.js, charts) share one React.
- **Bundled runtime** — the React runtime is esbuild-bundled into the jar and served at
  `/springreact/springreact.js`. Consumers ship no frontend files.

## Documentation & example

Beginner-friendly guides with copy-paste examples live in **[`docs/`](docs/README.md)** —
one page per feature, plus a full [Todo app tutorial](docs/11-tutorial-todo-app.md) and a
[How It Works](docs/12-how-it-works.md) deep dive. Start at
[Getting Started](docs/01-getting-started.md).

A complete **runnable example** is in **[`examples/todo`](examples/todo)** (validated form,
keyed list, shared layout with a live badge, two routes — all Kotlin, no frontend files):

```bash
cd examples/todo && gradle bootRun   # → http://localhost:8080
```

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

- **24 Spring integration tests** over the real `/live` WebSocket — live engine (DI,
  widgets, diffing), shell + routing, dynamic route params, broadcast (two clients), form
  validation, keyed reconciliation, authorization, async/redirect, 404, middleware, nested layouts, and error boundaries.
- **18 client unit tests** (vitest) — patch application, route resolution + matching, keyed
  ops — plus a TypeScript typecheck.

`npm install` and the client tests run automatically as Gradle tasks; no manual steps.

## Configuration (`spring.react.*`)

| Property                       | Default                       | Meaning                                  |
|--------------------------------|-------------------------------|------------------------------------------|
| `spring.react.title`           | `SpringReact`                 | default `<title>`                        |
| `spring.react.runtime-path`    | `/springreact/springreact.js` | URL of the bundled runtime               |
| `spring.react.allowed-origins` | `*`                           | `/live` WebSocket origin allowlist       |
| `spring.react.not-found-view`  | *(empty)*                     | component to render for 404s             |
| `spring.react.error-view`      | *(empty)*                     | component to render when render() throws |
| `spring.react.ssr`             | `true`                        | pre-render the initial screen into the HTML |
| `spring.react.stylesheets`     | *(empty)*                     | CSS URLs added as `<link>` to the shell  |
| `spring.react.scripts`         | *(empty)*                     | JS URLs (widget bundles) loaded after the runtime |

## Versions

Kotlin 2.1.0 · Spring Boot 3.4.1 · Java 21 toolchain · Gradle Kotlin DSL ·
node-gradle 7.1.0 (system Node) · React 18 (bundled).

## Roadmap

Shipped: server-side rendering (JVM-native, no Node),
Gradle plugin, project initializer (tools/create-springreact.sh), and Maven Central
publish wiring (signing + sources/javadoc jars, gated on credentials).

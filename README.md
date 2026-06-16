# SpringReact

**Build interactive websites by writing one language — Kotlin — with no separate
"frontend" to learn.**

> 🟢 **New to programming?** This page now starts gentle. SpringReact is a tool for people
> who know a *little* **Kotlin or Java**. If you've never coded, that's okay — learn some
> basic Kotlin first, then come back. (Looking for the precise technical pitch? It's in
> [For experienced developers](#for-experienced-developers) lower down.)

## What it does, in plain words

To make a modern website you normally write **two** programs and glue them together:

- a **backend** — runs on the *server* (an always-on computer that holds your data), and
- a **frontend** — runs in each visitor's *web browser*; the buttons and screens they see.

That's a lot to learn. **SpringReact removes the second one.** You write your screens once,
in Kotlin, on the server. SpringReact shows them in the browser and keeps them **live**:
when someone clicks, your Kotlin code runs and the screen updates by itself — no separate
frontend project, no JavaScript needed.

> 🍽️ **Analogy:** normally the kitchen (backend) and the waiters (frontend) coordinate with
> written tickets (glue code). SpringReact is like the kitchen controlling each diner's
> plate directly — you just cook (write Kotlin) and what people see updates itself.

## Your first screen (a click counter)

```kotlin
@LiveComponent("Home")          // 1. a screen we'll call "Home"
@Route("/")                     // 2. show it at the homepage address, "/"
class HomeScreen : ServerComponent {

    @LiveState var count = 0     // 3. a number we remember; starts at 0

    @LiveAction fun click() {    // 4. something the user can do…
        count = count + 1        //    …here, add 1 to count
    }

    override fun render(): UiNode =          // 5. what the screen looks like:
        div(
            h1("You clicked $count times"),  //    a heading showing the number
            button(onClick("click"), "Click me"),  // a button that calls click()
        )
}
```

**What you'd see:** open the site → "You clicked **0** times" and a button. Every click runs
`click()` on the server, `count` goes up, and the heading updates automatically.

Reading it line by line:

1. `@LiveComponent("Home")` — "this class is a screen named Home."
2. `@Route("/")` — "show this screen at the website's homepage."
3. `@LiveState var count = 0` — a value the screen remembers (its *state*). `var` means it
   can change; it starts at `0`.
4. `@LiveAction fun click() { ... }` — an action the browser can trigger (here, when the
   button is clicked). It changes `count`.
5. `render()` — describes what to show. `div`, `h1`, `button` are just HTML pieces written
   as Kotlin; `$count` drops the current number into the text.

That's the whole idea: **remember some state, define actions, and describe the screen.**
SpringReact handles the rest.

## Try it (5 minutes)

Follow **[Getting Started](docs/01-getting-started.md)** — it walks you through running your
very first screen step by step. Or run the ready-made example:

```bash
cd examples/todo && gradle bootRun   # then open http://localhost:8080
```

---

## For experienced developers

A **Kotlin Spring Boot framework** for building React UIs without leaving the JVM. You
write screens as **Kotlin server components**; the framework renders them to a React
element tree, streams it (and incremental patches) to a runtime it **bundles inside its own
jar**, and reconciles it with real React over **one WebSocket**. Like Thymeleaf: add the
dependency, write components, run. **No separate frontend project, no npm, no REST glue.**

### Quick start (Gradle plugin)

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

## Documentation

Beginner-friendly guides with copy-paste examples — one page per feature. New here? Read
them in order; or jump to what you need.

**Basics**
1. [Getting Started](docs/01-getting-started.md) — your first screen in 5 minutes
2. [Server Components](docs/02-server-components.md) — `@LiveComponent`, `@LiveState`, `@LiveAction`
3. [The HTML DSL](docs/03-html-dsl.md) — building UI in Kotlin
4. [Routing & Layouts](docs/04-routing-and-layouts.md) — `@Route`, dynamic params, nested layouts, 404
5. [Forms & Validation](docs/05-forms-and-validation.md) — typed forms with error messages

**Interactivity**
6. [Realtime Broadcast](docs/06-realtime-broadcast.md) — push updates to every user
7. [Custom Widgets](docs/07-custom-widgets.md) — drop real React components into a screen
8. [Authorization](docs/08-authorization.md) — guard actions by role
9. [Lists & Keys](docs/09-lists-and-keys.md) — efficient list updates
13. [Async, Loading & Redirects](docs/13-async-and-redirects.md) — slow work, server-side navigation
14. [Middleware](docs/14-middleware.md) — run logic before every action
15. [Lifecycle & Presence](docs/15-lifecycle-and-presence.md) — `onMount`/`onUnmount`, who's-online
16. [Internationalization](docs/16-i18n.md) — render in the user's language

**Styling, npm modules & rendering**
- 🎨 [**npm Modules & Tailwind**](docs/20-npm-modules-and-tailwind.md) — **use Tailwind, three.js, charts, any node module**
- [SSR & Styling](docs/19-ssr-and-styling.md) — server-rendered HTML + adding CSS

**Setup, ops & reference**
10. [Configuration](docs/10-configuration.md) — every `spring.react.*` setting
17. [The Gradle Plugin](docs/17-gradle-plugin.md) — one-line project setup
18. [Publishing](docs/18-publishing.md) — release to Maven Central
12. [How It Works](docs/12-how-it-works.md) — the architecture, end to end

**Tutorial**
11. [Build a Todo App](docs/11-tutorial-todo-app.md) — a complete app, step by step

## Examples

| Example | What it shows |
|---|---|
| [`examples/minimal`](examples/minimal) | The smallest app — one screen via the Gradle plugin |
| [`examples/todo`](examples/todo) | Full showcase: validated form, keyed list, quantity steppers, duplicate-name toast, filters, live badge, **Tailwind-style CSS** and a **three.js custom widget** |

```bash
cd examples/todo && gradle bootRun   # → http://localhost:8080
```

## Using Tailwind & npm modules (short version)

You write screens in Kotlin, so there's **no frontend project by default**. When you want
the npm ecosystem, there are two simple paths — full guide in
**[npm Modules & Tailwind](docs/20-npm-modules-and-tailwind.md)**.

**CSS / Tailwind** — Tailwind emits a stylesheet; your class names live in Kotlin
`cls("…")` strings, so point Tailwind at your `.kt` files and load the output:

```js
// tailwind.config.js
content: ['../src/main/kotlin/**/*.kt']
```
```bash
npx tailwindcss -i input.css -o src/main/resources/static/app.css --minify
```
```properties
spring.react.stylesheets=/app.css
```
```kotlin
div(cls("max-w-md mx-auto rounded-xl bg-slate-800 p-6"), h1(cls("text-2xl"), "Hi"))
```

**JS libraries (three.js, chart.js, …)** — a client-side lib goes in a small **widget
bundle**. The runtime exposes its React on `window.SpringReact.React`, so your bundle shares
it (alias `react` → a shim) instead of bundling a second copy:

```tsx
import React, { useEffect, useRef } from 'react'
import * as THREE from 'three'
function Cube() { /* … */ }
window.SpringReact.registerWidget('Cube', Cube)
```
```bash
esbuild src/widgets.tsx --bundle --alias:react=./react-shim.js --outfile=static/widgets.js
```
```properties
spring.react.scripts=/widgets.js
```
```kotlin
widget("Cube", attr("color", "#9b6dff"), attr("size", 180))   // in any screen
```

Trade-off: pure-Kotlin UI needs **zero npm**; the moment you use a JS library you need a
small bundle step *for that widget only* (you can't run JS libs without JS tooling). The
[`examples/todo`](examples/todo) About page renders a real three.js cube exactly this way.

## Module layout

```
SpringReact/                       (Kotlin, build.gradle.kts)
├── src/main/kotlin/com/vexora/springreact/
│   ├── jsc/        Html DSL, UiNode/Element/Text/Attr, ServerComponent, UiTreeDiff, UiHtml (SSR)
│   ├── live/       @LiveComponent/@LiveState/@LiveAction, registry, WebSocket handler,
│   │               LiveBroadcaster, LiveErrors, LiveContext, LiveInterceptor, auto-config
│   ├── web/        @Route/@Layout + RouteRegistry, ReactView/ReactViewResolver
│   └── autoconfigure/  ReactProperties, ReactRenderer (shell + SSR), ReactAutoConfiguration
├── src/test/kotlin/com/vexora/springreact/it/   integration tests — drive the real /live socket
├── gradle-plugin/  the `com.vexora.springreact` Gradle plugin (one-line setup)
└── client/         the bundled runtime (esbuild): ServerView, Router, hooks, patch
                    application, widget registry  (+ vitest unit tests)
```

## Build & test — one command

```bash
./gradlew build
```

Compiles Kotlin, esbuild-bundles the runtime into the jar, and runs **both** suites:

- **26 Spring integration tests** over the real `/live` WebSocket — live engine (DI,
  widgets, diffing), shell + routing, dynamic params, broadcast, forms, keyed reconciliation,
  authorization, async/redirect, 404, middleware, nested layouts, error boundaries, i18n,
  lifecycle, SSR, and custom assets.
- **24 client tests** (vitest, incl. jsdom browser-path tests) — patch application, routing,
  keyed ops, and full `ServerView`/`Router` rendering — plus a TypeScript typecheck.

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

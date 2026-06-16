<div align="center">

# SpringReact

**Build live, interactive web UIs in pure Kotlin — no separate frontend, no JavaScript, no REST glue.**

[![CI](https://github.com/VexoraWebServices/springReact/actions/workflows/ci.yml/badge.svg)](https://github.com/VexoraWebServices/springReact/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-6DB33F?logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

[Quick start](#quick-start) · [Documentation](#documentation) · [Examples](#examples) · [How it works](docs/12-how-it-works.md)

</div>

---

SpringReact is a **Spring Boot framework** that lets you write your entire web UI as **Kotlin
server components**. You describe a screen in Kotlin; the framework renders it, serves it, and
keeps it **live over a single WebSocket** — when a user interacts, your Kotlin code runs and
only the changed parts of the page update. It bundles its own React runtime inside the jar, so
there is **no separate frontend project and nothing to install on the client**.

> 🟢 **New to programming?** Here's the idea in plain words: a modern website is usually *two*
> programs — a **backend** (on the server, holds your data) and a **frontend** (in the
> browser, the buttons people see) — plus glue to connect them. SpringReact removes the
> second one: you write everything once in Kotlin and it shows up, live, in the browser.
> It does assume you know a *little* Kotlin or Java — if you're brand new, start with
> [Getting Started](docs/01-getting-started.md).

## Table of Contents

- [Why SpringReact](#why-springreact)
- [Quick start](#quick-start)
- [How a screen looks](#how-a-screen-looks)
- [Features](#features)
- [Documentation](#documentation)
- [Examples](#examples)
- [Tailwind & npm modules](#tailwind--npm-modules)
- [Configuration](#configuration)
- [Project structure](#project-structure)
- [Building from source](#building-from-source)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

## Why SpringReact

| | Classic SPA + REST | Thymeleaf / server templates | **SpringReact** |
|---|---|---|---|
| Languages | Kotlin **+** JS/TS | Kotlin + HTML templates | **Kotlin only** |
| Frontend project | Separate (npm, bundler) | None | **None** (runtime bundled in the jar) |
| Client ↔ server | Hand-written REST API | Full page reloads | **Automatic, over one WebSocket** |
| Live updates | DIY (state, fetch, sockets) | ✗ | **Built-in** (only changed DOM updates) |
| SEO / first paint | CSR (often empty source) | ✓ | **✓ server-side rendered** |

You get React's interactivity with the simplicity of server-side development — one language,
one process, one deployable jar.

## Quick start

Add the Gradle plugin — it configures Kotlin, Spring Boot, dependency management, and the
framework:

```kotlin
// build.gradle.kts
plugins {
    id("com.vexora.springreact") version "0.1.0"
}
```

Or scaffold a new project from scratch:

```bash
./tools/create-springreact.sh my-app com.acme.myapp
cd my-app && ./gradlew bootRun        # → http://localhost:8080
```

See [The Gradle Plugin](docs/17-gradle-plugin.md) and [Getting Started](docs/01-getting-started.md).

## How a screen looks

```kotlin
@LiveComponent("Home")          // a screen named "Home"
@Route("/")                     // served at the homepage "/"
class HomeScreen : ServerComponent {

    @LiveState var count = 0                 // state lives on the server

    @LiveAction fun click() { count++ }      // an action the user can trigger

    override fun render(): UiNode =          // what the screen looks like
        div(
            h1("You clicked $count times"),
            button(onClick("click"), "Click me"),
        )
}
```

Open the page → "You clicked **0** times". Each click runs `click()` on the server, `count`
goes up, and the heading updates instantly — no API to write, no client state to manage.
New to this? Every line is explained in [Getting Started](docs/01-getting-started.md).

## Features

- **Server components** — `@LiveComponent` + `render()` in Kotlin, with full Spring DI. State
  lives in the JVM (`@LiveState`); events are `@LiveAction`s.
- **One WebSocket transport** — no REST, no client store. Full tree on mount, minimal **diff
  patches** after that.
- **Routing & client navigation** — `@Route("/path", layout, title)`; **dynamic params**
  (`/users/{id}` → `@LiveParam`), per-route titles, client-side nav (no full reload).
- **Layouts (incl. nested)** — `Html.slot()`; `@Layout(parent="…")` nests layouts; they stay
  mounted while the inner screen swaps.
- **Realtime broadcast** — `LiveBroadcaster.broadcast("Component")` re-renders every connected
  client (live dashboards, presence, chat).
- **Forms + validation** — `onSubmit` binds named fields to a typed Kotlin DTO, validated with
  Bean Validation before your action runs.
- **Keyed reconciliation** — `key()` on list children → minimal patches on reorder/insert/remove.
- **Custom widgets** — drop real React components (charts, three.js, maps) into a screen with
  `widget("Name", …)`; logic stays on the server.
- **Authorization** — `@LiveAuthorize("ROLE")` + a pluggable `LiveSecurity` bean (bridge to
  Spring Security).
- **Async, loading & redirects** — `LiveContext` gives an action a `handle()` to push updates
  after background work, plus `redirect("/path")`.
- **404 & error boundaries** — render your own components for unknown URLs and render failures.
- **Middleware** — `LiveInterceptor` beans run before every action (logging, tenancy, rate limits).
- **Lifecycle & context** — `onMount`/`onUnmount` (reliable on disconnect) and access to
  headers/cookies/principal/locale.
- **i18n** — locale from `Accept-Language` + Spring `MessageSource`.
- **Server-side rendering** — initial screen + layouts pre-rendered to HTML on the JVM (no
  Node) for SEO and fast first paint.
- **Bundled runtime** — the React runtime is esbuild-bundled into the jar; consumers ship no
  frontend files.

## Documentation

One page per feature, beginner-friendly with copy-paste examples. New here? Read in order; or
jump to what you need.

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
- [**Frontend for a REST API**](docs/21-frontend-for-an-api.md) — use SpringReact with your existing Spring Boot API

**Tutorial**
11. [Build a Todo App](docs/11-tutorial-todo-app.md) — a complete app, step by step

## Examples

| Example | What it shows |
|---|---|
| [`examples/minimal`](examples/minimal) | The smallest app — one screen via the Gradle plugin |
| [`examples/todo`](examples/todo) | Full showcase: validated form, keyed list, quantity steppers, duplicate-name toast, filters, live badge, custom CSS, and a **three.js custom widget** |
| [`examples/todo-api`](examples/todo-api) | **SpringReact as the frontend for a REST API** — a `/api/todos` JSON API consumed by the UI via `RestClient` |

```bash
./gradlew publishToMavenLocal        # make the framework available locally
cd examples/todo && gradle bootRun   # → http://localhost:8080
```

## Tailwind & npm modules

You write screens in Kotlin, so there is **no frontend project by default**. When you want the
npm ecosystem there are two simple paths — full guide:
[**npm Modules & Tailwind**](docs/20-npm-modules-and-tailwind.md).

**CSS / Tailwind** — Tailwind emits a stylesheet; your class names live in Kotlin `cls("…")`
strings, so point Tailwind at your `.kt` files and load the output:

```js
// tailwind.config.js — scan your Kotlin screens for class names
content: ['../src/main/kotlin/**/*.kt']
```
```properties
spring.react.stylesheets=/app.css     # built by `npx tailwindcss -o .../static/app.css`
```

**JS libraries (three.js, chart.js, …)** — a client-side lib goes in a small **widget bundle**.
The runtime exposes its React on `window.SpringReact.React`, so your bundle shares it instead
of shipping a second copy:

```tsx
import * as THREE from 'three'
function Cube() { /* … */ }
window.SpringReact.registerWidget('Cube', Cube)
```
```properties
spring.react.scripts=/widgets.js      # built with esbuild --alias:react=./react-shim.js
```
```kotlin
widget("Cube", attr("color", "#9b6dff"), attr("size", 180))   // use it from Kotlin
```

> Trade-off: pure-Kotlin UI needs **zero npm**; the moment you use a JS library you need a
> small bundle step *for that widget only*. The [`examples/todo`](examples/todo) About page
> renders a real three.js cube exactly this way.

## Configuration

All settings live under `spring.react.*` in `application.properties` / `.yml`. Everything has a
sensible default — zero config required.

| Property | Default | Meaning |
|---|---|---|
| `spring.react.title` | `SpringReact` | default `<title>` |
| `spring.react.ssr` | `true` | pre-render the initial screen into the HTML |
| `spring.react.stylesheets` | *(empty)* | CSS URLs added as `<link>` to the shell |
| `spring.react.scripts` | *(empty)* | JS URLs (widget bundles) loaded after the runtime |
| `spring.react.not-found-view` | *(empty)* | component to render for 404s |
| `spring.react.error-view` | *(empty)* | component to render when `render()` throws |
| `spring.react.allowed-origins` | `*` | `/live` WebSocket origin allowlist |
| `spring.react.runtime-path` | `/springreact/springreact.js` | URL of the bundled runtime |

## Project structure

```
SpringReact/                       (Kotlin, build.gradle.kts)
├── src/main/kotlin/com/vexora/springreact/
│   ├── jsc/        Html DSL, UiNode/Element/Text/Attr, ServerComponent, UiTreeDiff, UiHtml (SSR)
│   ├── live/       @LiveComponent/@LiveState/@LiveAction, WebSocket handler, LiveBroadcaster,
│   │               LiveContext, LiveInterceptor, LiveSecurity, auto-config
│   ├── web/        @Route/@Layout + RouteRegistry, ReactView/ReactViewResolver
│   └── autoconfigure/  ReactProperties, ReactRenderer (shell + SSR), ReactAutoConfiguration
├── src/test/kotlin/  integration tests — drive the real /live WebSocket
├── gradle-plugin/    the `com.vexora.springreact` Gradle plugin (one-line setup)
├── client/           bundled runtime (esbuild): ServerView, Router, hooks, patch apply, widgets
├── examples/         minimal + todo apps
└── docs/             one guide per feature
```

## Building from source

**Requirements:** JDK 21, Node 22+. One command builds everything and runs both test suites:

```bash
./gradlew build
```

- **26 Spring integration tests** drive the real `/live` WebSocket (DI, routing, forms,
  broadcast, keyed diffing, auth, SSR, …).
- **24 client tests** (vitest + jsdom) cover patch application, routing, and full
  `ServerView`/`Router` rendering — plus a TypeScript typecheck.

The client runtime is esbuild-bundled into the jar automatically; there are no manual npm
steps. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Roadmap

- ✅ Server-side rendering (JVM-native, no Node)
- ✅ Gradle plugin + project initializer
- ✅ Custom widgets sharing the framework's React (three.js, charts)
- ✅ Maven Central publish wiring (signing + sources/javadoc, gated on credentials)
- ⬜ Publish to Maven Central & the Gradle Plugin Portal
- ⬜ Streaming SSR for fully server-rendered widgets

## Contributing

Contributions are welcome — see [CONTRIBUTING.md](CONTRIBUTING.md). In short: `./gradlew build`
must be green, framework code is Kotlin-only, and every feature ships with a doc page.

## License

[Apache License 2.0](LICENSE).

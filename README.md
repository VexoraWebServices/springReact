# SpringReact — Java/Kotlin Server Components for Spring Boot

A Spring Boot starter for a new view technology: **author your React screens in Java or
Kotlin**, render them on the JVM, and stream the React element tree — and **incremental
patches** — to a universal client over **one WebSocket**.

Think *Thymeleaf, but the template is type-safe Java/Kotlin and the renderer is real
React.* No per-screen `.tsx`, no REST endpoints, no client state store. One process, one
port, one JAR.

```java
@LiveComponent("Home")
public class HomeView implements ServerComponent {

    private final TodoService todos;            // full Spring DI
    public HomeView(TodoService todos) { this.todos = todos; }

    @LiveState int count = 0;                    // state lives in the JVM
    @LiveAction void increment() { count++; }

    public UiNode render() {                     // your "template", in Java
        return div(cls("card"),
            h1("Hello"),
            button(onClick("increment"), "Count: " + count),
            widget("StarRating", attr("value", count), attr("action", "increment")));
    }
}
```

```kotlin
@LiveComponent("Greet")
class GreetView : ServerComponent {
    @field:LiveState var who = "world"
    @LiveAction fun setName(v: String) { who = v }
    override fun render(): UiNode =
        div(cls("card"), h1("Hi, $who"),
            input(value(who), onChangeValue("setName")))
}
```

## What's in the box

| Piece | What it does |
|---|---|
| **`io.springreact.jsc`** | The Java/Kotlin "JSX" DSL (`Html`), `UiNode` tree, `ServerComponent`, and `UiTreeDiff` (server-side tree diffing). |
| **`io.springreact.live`** | The single-WebSocket transport: `@LiveComponent`/`@LiveState`/`@LiveAction`, component registry, the `/live` handler. |
| **`io.springreact.web`** | Thymeleaf-style `ReactViewResolver`/`ReactView` that render the HTML shell. |
| **`io.springreact.autoconfigure`** | Auto-config + Vite manifest/dev-server integration. |
| **`client/`** | The universal client runtime (npm package `@springreact/client`): `ServerView`, the one-WebSocket hooks, patch application, and the custom-widget registry. |

## How it works

```
Browser                          Spring Boot (one process, one port)
  │  GET /                        ┌──────────────────────────────────────┐
  ├──────────────────────────────►│ @Controller returns "Home"           │
  │  HTML shell: <ServerView/>     │ ReactViewResolver renders the shell  │
  │◄──────────────────────────────┤                                      │
  │  ws://…/live   (ONE socket)    │ LiveWebSocketHandler                 │
  │  mount "Home" ────────────────►│  → create HomeView (DI'd bean)       │
  │◄── tree (full React VDOM) ─────┤  → HomeView.render() → UiNode tree   │
  │  call increment ──────────────►│  → @LiveAction mutates @LiveState    │
  │◄── patch (minimal diff) ───────┤  → re-render + UiTreeDiff vs last    │
  └────────────────────────────────└──────────────────────────────────────┘
        ServerView applies patches and reconciles with React
```

- **Diffing.** Mount sends the full tree once; every later update sends only a patch
  (`text` / `props` / `insert` / `remove` / `replace` ops addressed by child-index path).
  The client applies it to a shadow tree and re-renders.
- **Custom widgets.** `widget("StarRating", attr(...))` renders a real client React
  component registered via `registerWidget("StarRating", StarRating)` — rich UI (charts,
  canvas, animations) embedded in a Java/Kotlin screen, with a `call` to fire server
  actions. Logic stays on the server.
- **DI.** `@LiveComponent` is a prototype `@Component`; constructor-inject any bean.
- **Java *and* Kotlin.** Identical API; in Kotlin annotate state with `@field:LiveState`.

## Using it in your app

**1. Backend** — add the starter, write screens:

```java
@Controller
class Routes {
    @GetMapping("/") String home() { return "Home"; }   // → @LiveComponent("Home")
}
```

**2. Frontend** — one tiny entry; the screens live in Java/Kotlin:

```tsx
import { createRoot } from 'react-dom/client'
import { ServerView, registerWidget } from '@springreact/client'
import StarRating from './StarRating'          // your custom widgets (optional)

registerWidget('StarRating', StarRating)
createRoot(document.getElementById('root')!).render(<ServerView name={window.__VIEW__} />)
```

Build the frontend with Vite (backend-integration mode); the starter serves the shell and
auto-detects DEV (Vite dev server) vs PROD (hashed assets from the build manifest), and
can manage the Vite process for you in DEV (`spring.react.manage-dev-server`).

## Build & test

```bash
./gradlew test      # boots Spring, drives /live — verifies Java + Kotlin, diffing, widgets, DI
./gradlew build     # produces the plugin jar + sources jar
(cd client && npm install && npm run typecheck)   # verifies the client runtime
```

### What the tests prove (`src/test`)

- mount `JavaCounter` → a **full tree** with the injected `GreetingService` output, the
  `Count: 0` text, and a `$widget` node (`StarRating`, `value:0`).
- `increment` → a **patch** carrying only `Count: 1` (the unchanged subtree is *not* resent).
- `rate(5)` → a **patch** setting only the widget's `value:5`.
- `KotlinGreet` mounts and patches the same way — same plugin, Kotlin source.

## Configuration (`application.properties`)

| Property                          | Default                 | Meaning                              |
|-----------------------------------|-------------------------|--------------------------------------|
| `spring.react.mode`               | `AUTO`                  | `AUTO` \| `DEV` \| `PROD`             |
| `spring.react.dev-server-url`     | `http://localhost:5173` | Vite dev server (DEV)                |
| `spring.react.manage-dev-server`  | `true`                  | Spring starts/stops Vite in DEV      |
| `spring.react.frontend-dir`       | `frontend`              | where `package.json` lives           |
| `spring.react.entry`              | `src/main.tsx`          | Vite entry (`rollupOptions.input`)   |
| `spring.react.title`              | `Spring React`          | default `<title>`                    |

## Roadmap

- `key`-based list reconciliation for stable reorder/animation.
- Broadcast re-renders (push to all mounted clients — live dashboards, presence).
- Publish `@springreact/client` and the starter to a registry.

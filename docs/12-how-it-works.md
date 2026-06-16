# 12. How It Works

A peek under the hood. You don't need this to use SpringReact, but it helps when debugging.

## The big picture

```
Browser                                   Spring Boot (one process, one port)
  │  GET /users                           ┌───────────────────────────────────────┐
  ├──────────────────────────────────────►│ @Route("/users") → render HTML shell  │
  │   <html> … window.__VIEW__="Users"     │  (inlines __VIEW__, __MODEL__,        │
  │           __ROUTES__={…}               │   __ROUTES__) + <script> the runtime  │
  │   <script src="/springreact/...js">    │                                       │
  │◄──────────────────────────────────────┤                                       │
  │                                        │                                       │
  │  open ws://…/live  (ONE socket)        │ LiveWebSocketHandler                  │
  │  mount {c:"Users"} ───────────────────►│  create UsersScreen (DI'd bean)       │
  │◄── {t:"tree", tree:{…}} ───────────────┤  UsersScreen.render() → UiNode tree   │
  │  call {action:"remove", args:[3]} ────►│  @LiveAuthorize? → @LiveAction runs   │
  │◄── {t:"patch", ops:[…]} ───────────────┤  re-render, diff vs last tree         │
  └────────────────────────────────────────└───────────────────────────────────────┘
        the bundled runtime applies the patch and reconciles with React
```

## The pieces

**`jsc` — the UI model**
- `Html` — the DSL functions you call (`div`, `button`, …).
- `UiNode` (`Element` / `Text`) — the tree your `render()` returns.
- `UiTreeDiff` — computes the minimal patch between the old and new tree (index- or
  key-based).

**`live` — the transport**
- `@LiveComponent` / `@LiveState` / `@LiveAction` — your component contract.
- `LiveComponentRegistry` — finds components and creates fresh, injected instances.
- `LiveWebSocketHandler` — the single `/live` endpoint. Holds per-session instances, runs
  actions, sends tree/patch, and broadcasts.
- `LiveBroadcaster`, `LiveErrors`, `LiveSecurity`/`@LiveAuthorize` — broadcast, form
  validation, and authorization.

**`web` — routing + shell**
- `@Route` / `RouteRegistry` — URL → component map, also injected into the page as
  `window.__ROUTES__`.
- `ReactView` / `ReactViewResolver` — lets MVC controllers return a view name too.

**`autoconfigure` — wiring**
- `ReactRenderer` — builds the HTML shell.
- `ReactProperties` — the `spring.react.*` settings.
- Auto-configuration registers everything with zero setup.

**`client/` — the bundled runtime** (esbuild → one JS file inside the jar)
- `ServerView` — turns a UiNode tree into React elements; applies patches.
- `Router` — client-side navigation + layouts.
- `live.ts` — the single WebSocket, the hooks, and `applyOps` (patch application).
- `widgets` — the custom-component registry.

## Message protocol (the `/live` socket)

Client → server:
- `{ t:"mount", id, c }` — mount component `c` with client instance `id`
- `{ t:"call", id, action, args }` — run an action
- `{ t:"unmount", id }`

Server → client:
- `{ t:"tree", id, tree }` — full UI tree (first render)
- `{ t:"patch", id, ops }` — minimal changes (`text`/`props`/`insert`/`remove`/`replace`/`keyed`)
- `{ t:"state", id, state }` — raw state (for non-`ServerComponent` live components)
- `{ t:"error", id, message }` — an action failed or was unauthorized

## Why one WebSocket?

State lives on the server, so the client never needs a REST API or its own store — it only
needs a channel to send events and receive UI updates. One socket carries all components on
the page, multiplexed by `id`.

## Where to look when debugging

- Browser devtools → Network → WS → the `/live` frames show every message.
- Server logs: failed actions log a warning with the action name and error.
- `./gradlew build` runs the full test suite (server + client) — a good regression net.

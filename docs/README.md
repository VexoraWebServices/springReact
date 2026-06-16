# SpringReact Documentation

New here? Read these in order. Every page is written for beginners, with copy-paste
examples you can run.

1. [Getting Started](01-getting-started.md) — your first screen in 5 minutes
2. [Server Components](02-server-components.md) — `@LiveComponent`, `@LiveState`, `@LiveAction`
3. [The HTML DSL](03-html-dsl.md) — building UI in Kotlin/Java
4. [Routing & Layouts](04-routing-and-layouts.md) — `@Route`, navigation, shared layouts
5. [Forms & Validation](05-forms-and-validation.md) — typed forms with error messages
6. [Realtime Broadcast](06-realtime-broadcast.md) — push updates to every user
7. [Custom Widgets](07-custom-widgets.md) — drop real React components into a screen
8. [Authorization](08-authorization.md) — guard actions by role
9. [Lists & Keys](09-lists-and-keys.md) — efficient list updates
10. [Configuration](10-configuration.md) — every `spring.react.*` setting
11. [Tutorial: Todo App](11-tutorial-todo-app.md) — build a complete app step by step
12. [How It Works](12-how-it-works.md) — the architecture, end to end
13. [Async, Loading & Redirects](13-async-and-redirects.md) — slow work and server-side navigation
14. [Middleware](14-middleware.md) — run logic before every action
15. [Lifecycle & Presence](15-lifecycle-and-presence.md) — onMount/onUnmount, who's-online
16. [Internationalization](16-i18n.md) — render in the user's language
17. [The Gradle Plugin](17-gradle-plugin.md) — one-line project setup
18. [Publishing](18-publishing.md) — release to Maven Central

## The 30-second mental model

```
You write a Kotlin class (a "server component").
        │  its render() returns a UI tree, built with the Html DSL
        ▼
SpringReact sends that tree to the browser over ONE WebSocket.
        │  a tiny runtime (bundled in the jar) draws it with React
        ▼
The user clicks something → it calls a method on your class (a "@LiveAction").
        │  your method changes state (a "@LiveState" field)
        ▼
SpringReact re-renders, sends only what changed (a patch), the UI updates.
```

No REST endpoints. No separate frontend project. No npm. Your whole UI lives in the JVM.

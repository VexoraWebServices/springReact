# 10. Configuration

All settings live under `spring.react.*` in `application.properties` / `application.yml`.
Everything has a sensible default — you can run with **zero** config.

| Property | Default | What it does |
|---|---|---|
| `spring.react.title` | `SpringReact` | The `<title>` of the HTML shell. |
| `spring.react.runtime-path` | `/springreact/springreact.js` | URL the bundled runtime is served from. Change only if you serve it elsewhere (e.g. a CDN). |
| `spring.react.allowed-origins` | `*` | Allowed origins for the `/live` WebSocket. **Lock this down in production.** |
| `spring.react.not-found-view` | *(empty)* | Component name to render for unknown URLs (404). Empty = Spring's default error page. |

## Examples

`application.properties`:

```properties
spring.react.title=My App
spring.react.allowed-origins=https://app.example.com
```

`application.yml`:

```yaml
spring:
  react:
    title: My App
    allowed-origins:
      - https://app.example.com
      - https://admin.example.com
```

## Production checklist

- [ ] Set `spring.react.allowed-origins` to your real domain(s) — not `*`.
- [ ] Put the app behind HTTPS (so the client uses `wss://` automatically).
- [ ] Add a `LiveSecurity` bean and Spring Security if you guard actions
      (see [Authorization](08-authorization.md)).
- [ ] Next: [Tutorial: Todo App](11-tutorial-todo-app.md)

# 19. Server-Side Rendering & Styling

## Server-Side Rendering (SSR)

By default, SpringReact **pre-renders** the initial screen (and its layouts) into the HTML
the server sends. So "View Source", search-engine crawlers, link-preview bots, and the very
first paint all show **real content** — not an empty `<div id="root">`.

It needs **no Node and no React-on-the-server**: because the framework owns the VDOM format,
it serializes the component tree to HTML directly on the JVM. The browser runtime then takes
over the same tree for interactivity (it's seeded from `window.__SSR__`, so there's no
flash).

### It just works

Write components as usual — SSR is on by default. Visit any route and View Source shows:

```html
<div id="root"><div class="app"><header class="nav">…</header>
  <main><div class="card"><h1>Todos</h1>…</div></main></div></div>
```

### Turn it off

```properties
spring.react.ssr=false
```

Then `#root` ships empty and everything renders on the client (the old behaviour).

### Notes & limits

- **Custom widgets** (`widget("…")`) can't be pre-rendered (they're client React); they
  appear as an empty placeholder in the SSR HTML and fill in on load.
- **Dynamic route params** aren't bound during SSR (the initial HTML uses defaults); the
  client mounts with the real param immediately after. Title/description *are* server-rendered.
- SSR never breaks a page: if a component throws during pre-render, the server falls back to
  shipping an empty root and the client renders normally.

## Styling

The shell has no opinion about CSS. Add your own stylesheet(s) with:

```properties
spring.react.stylesheets=/app.css
```

(Comma-separate for several: `spring.react.stylesheets=/base.css,/theme.css`.)

Put the file in `src/main/resources/static/` and Spring Boot serves it. Reference your
classes from components via `cls("…")`:

```kotlin
div(cls("card"), h1("Hello"))
```

```css
/* src/main/resources/static/app.css */
.card { background: #1a2238; border-radius: 14px; padding: 28px; }
```

The full [`examples/todo`](../examples/todo) app ships a complete modern stylesheet
(`static/app.css`) wired this way — a good starting point to copy.

> Want utility CSS (Tailwind etc.) or a component library? Build your CSS however you like
> and point `spring.react.stylesheets` at the output, or add a `<link>` to a CDN. Custom
> interactive widgets (charts, etc.) go through [Custom Widgets](07-custom-widgets.md).

## TODO checklist

- [ ] Confirm View Source shows your content (SSR on by default)
- [ ] Add `static/app.css` and set `spring.react.stylesheets=/app.css`
- [ ] Style your component classes

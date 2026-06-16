# Todo widgets (three.js)

A custom-widget bundle for the SpringReact todo example. It registers the `Cube` widget
(a rotating three.js cube) used on the About page.

## Build

```bash
npm install
npm run build      # → ../src/main/resources/static/widgets.js
```

Then run the app (`cd .. && gradle bootRun`) and open http://localhost:8080/about.

The build aliases `react` to `react-shim.js`, which re-exports
`window.SpringReact.React` — so this bundle shares the framework's React instead of
bundling its own. The app loads it via `spring.react.scripts=/widgets.js`.

See [docs/20-npm-modules-and-tailwind.md](../../../docs/20-npm-modules-and-tailwind.md).

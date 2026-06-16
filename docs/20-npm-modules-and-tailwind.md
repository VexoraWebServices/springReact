# 20. Using npm Modules (three.js, charts…) & Tailwind

SpringReact apps have **no frontend project** by default — you write Kotlin. But you can
still use the npm ecosystem when you need it. There are two cases, and they're different:

- **CSS / Tailwind** → produces a stylesheet → load it with `spring.react.stylesheets`.
  No JavaScript involved.
- **JS libraries (three.js, chart.js, a date picker, an editor…)** → these are client
  React/JS, so they go through a **custom widget bundle** loaded with
  `spring.react.scripts`.

---

## Tailwind (and any CSS framework)

Tailwind just scans your files for class names and emits a CSS file. Your class names live
in Kotlin `cls("…")` strings, so point Tailwind's `content` at your `.kt` files.

**1. Install + configure** (in a small `frontend-css/` folder, or anywhere):

```js
// tailwind.config.js
export default {
  content: ['../src/main/kotlin/**/*.kt'],   // scan your Kotlin screens for classes
  theme: { extend: {} },
  plugins: [],
}
```

```css
/* input.css */
@tailwind base;
@tailwind components;
@tailwind utilities;
```

**2. Build into `static/`:**

```bash
npx tailwindcss -i input.css -o ../src/main/resources/static/app.css --minify
# add --watch during development
```

**3. Reference it:**

```properties
spring.react.stylesheets=/app.css
```

**4. Use Tailwind classes from Kotlin:**

```kotlin
div(cls("max-w-md mx-auto rounded-xl bg-slate-800 p-6 shadow-lg"),
    h1(cls("text-2xl font-bold text-indigo-400"), "Hello"))
```

That's it — same approach for Bootstrap, Bulma, or hand-written CSS (it works today with
just `spring.react.stylesheets`; the `examples/todo` app ships a plain `app.css` this way).

---

## JS libraries via custom widgets (e.g. three.js)

Anything interactive and client-side — a 3D scene, a chart, a map, a rich editor — is a
**custom widget**: a React component you write, bundle, register, and then drop into a
Kotlin screen with `widget("Name", …)`. Logic stays on the server; the widget is the
client-side piece.

The one important detail: your bundle must **share SpringReact's React** (not bundle its
own), or you'll hit "two copies of React / invalid hook call". SpringReact exposes its
React on `window.SpringReact.React`; you alias `react` to it.

### 1. A widget that uses an npm module

```bash
mkdir widgets && cd widgets
npm init -y
npm i three
npm i -D esbuild
```

```tsx
// widgets/src/widgets.tsx
import React, { useEffect, useRef } from 'react'
import * as THREE from 'three'

function Cube({ color = '#6d8bff', size = 160 }) {
  const ref = useRef<HTMLDivElement>(null)
  useEffect(() => {
    const el = ref.current!
    const scene = new THREE.Scene()
    const camera = new THREE.PerspectiveCamera(60, 1, 0.1, 100); camera.position.z = 3
    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
    renderer.setSize(size, size); el.appendChild(renderer.domElement)
    const cube = new THREE.Mesh(
      new THREE.BoxGeometry(1.4, 1.4, 1.4),
      new THREE.MeshStandardMaterial({ color }))
    scene.add(cube, new THREE.AmbientLight(0xffffff, 0.6))
    let raf = 0
    const tick = () => { cube.rotation.x += .012; cube.rotation.y += .016
      renderer.render(scene, camera); raf = requestAnimationFrame(tick) }
    tick()
    return () => { cancelAnimationFrame(raf); renderer.dispose(); el.removeChild(renderer.domElement) }
  }, [color, size])
  return <div ref={ref} style={{ width: size, height: size }} />
}

// Register it so Kotlin screens can use widget("Cube", ...)
window.SpringReact.registerWidget('Cube', Cube)
```

### 2. The React shim (share SpringReact's React)

```js
// widgets/react-shim.js
const React = window.SpringReact.React
export default React
export const { useState, useEffect, useRef, useMemo, useCallback, createElement, Fragment } = React
```

### 3. Bundle it into `static/` (esbuild, aliasing react to the shim)

```json
// widgets/package.json — "build" script
"build": "esbuild src/widgets.tsx --bundle --format=iife --minify --jsx=transform --jsx-factory=React.createElement --jsx-fragment=React.Fragment --alias:react=./react-shim.js --outfile=../src/main/resources/static/widgets.js"
```

```bash
npm run build   # → src/main/resources/static/widgets.js  (three.js bundled, React shared)
```

### 4. Load the bundle (after the runtime, automatically)

```properties
spring.react.scripts=/widgets.js
```

SpringReact injects it **after** its runtime, so `window.SpringReact.registerWidget` and
`window.SpringReact.React` already exist when your bundle runs.

### 5. Use it from Kotlin

```kotlin
widget("Cube", attr("color", "#9b6dff"), attr("size", 180))
```

Props (`color`, `size`) arrive on the component; it also receives a `call(action, …)` to
invoke server actions. See the working example in
[`examples/todo`](../examples/todo) — the **About page** renders this exact three.js cube
(`widgets/` builds the bundle; `application.properties` wires `spring.react.scripts`).

> SSR note: a widget can't be pre-rendered on the JVM (it's client React), so it shows as
> an empty placeholder in the initial HTML and mounts on load — see
> [SSR & Styling](19-ssr-and-styling.md).

### Why the shim?

`window.SpringReact.React` is the same React the runtime uses. Aliasing `react` to it means
your three.js widget and the framework share one React instance and one virtual DOM — hooks
work, context works, and your bundle is ~600KB smaller (no second React).

## TODO checklist

- [ ] CSS: build with Tailwind (scan `**/*.kt`) → `spring.react.stylesheets`
- [ ] JS: write a widget, add `react-shim.js`, bundle with esbuild `--alias:react=./react-shim.js`
- [ ] `spring.react.scripts=/widgets.js`, then `widget("Name", …)` in Kotlin

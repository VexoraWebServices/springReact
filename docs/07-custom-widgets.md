# 7. Custom Widgets

The built-in DSL covers normal HTML. When you need a **real React component** — a chart, a
map, a date picker, a canvas animation — use a **widget**: you write the React component
once, register it by name, and drop it into any server-authored screen. Logic still lives
on the server.

## Step 1 — write a React component

Put it in a small frontend file. It receives the props you pass from the server, plus a
`call(action, ...args)` to invoke server actions.

```tsx
// widgets/StarRating.tsx
type Props = { value: number; action: string; call: (a: string, ...x: unknown[]) => void }

export default function StarRating({ value, action, call }: Props) {
  return (
    <div className="stars">
      {[1, 2, 3, 4, 5].map((n) => (
        <span key={n} className={value >= n ? 'on' : ''} onClick={() => call(action, n)}>★</span>
      ))}
    </div>
  )
}
```

## Step 2 — register it

In a tiny entry script that you load after the SpringReact runtime:

```tsx
import StarRating from './widgets/StarRating'
window.SpringReact.registerWidget('StarRating', StarRating)
```

> Only apps that use custom widgets need a small frontend build step. The core framework
> still needs none.

## Step 3 — use it from a server component

```kotlin
@LiveComponent("Movie")
class MovieScreen : ServerComponent {
    @LiveState var rating = 0
    @LiveAction fun rate(stars: Int) { rating = stars }

    override fun render(): UiNode =
        div(cls("card"),
            h2("Rate this movie"),
            widget("StarRating",
                attr("value", rating),       // becomes the `value` prop
                attr("action", "rate")),     // becomes the `action` prop
            p("You rated: $rating"))
}
```

Clicking a star calls `call("rate", n)`, which runs your server `rate(n)` action, which
updates `rating`, which re-renders — and the new value flows back into the widget.

## How it works

```
Java/Kotlin:  widget("StarRating", attr("value", 3), attr("action", "rate"))
        │ serialized
        ▼
{ tag: "$widget", props: { name: "StarRating", value: 3, action: "rate" } }
        │ the runtime looks up the registry
        ▼
<StarRating value={3} action="rate" call={...} />
```

## TODO checklist

- [ ] Write a React component that takes props + `call`
- [ ] `window.SpringReact.registerWidget("Name", Comp)`
- [ ] `widget("Name", attr("prop", value))` in a server component
- [ ] Wire a click to a server action via `call`
- [ ] Next: [Authorization](08-authorization.md)

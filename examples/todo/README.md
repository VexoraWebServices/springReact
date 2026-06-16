# SpringReact Example — Todo App

A complete, runnable app: validated form, keyed list, shared layout with a live count
badge, two routes. **All Kotlin, no frontend files.**

```bash
cd examples/todo
gradle bootRun        # or ./gradlew bootRun from the repo with a wrapper
```

Open <http://localhost:8080>:

- Add a todo (empty text shows a validation error)
- Toggle / remove items — instant updates
- The "N items" badge is shared — open two tabs and watch both change
- Click **About** — the header stays put (layout), only the body swaps (client nav)

It depends on the framework from this repo via a Gradle **composite build**
(`includeBuild("../..")`), so you don't need to publish anything first.

See the [full tutorial](../../docs/11-tutorial-todo-app.md) for a step-by-step walkthrough.

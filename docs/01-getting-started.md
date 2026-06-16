# 1. Getting Started

> Goal: a working "Hello, counter" screen in about 5 minutes.

## Step 1 — add the dependency

`build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.vexora.springreact:SpringReact:0.1.0")
}
```

That's it for the frontend. SpringReact bundles its React runtime **inside the jar** and
serves it automatically. You do **not** create a `frontend/` project and you do **not**
run `npm`.

## Step 2 — write a screen

A "screen" is a normal class that implements `ServerComponent` and renders UI in Kotlin.

```kotlin
import com.vexora.springreact.jsc.Html.*
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.*
import com.vexora.springreact.web.Route

@LiveComponent("Home")        // the component's name
@Route("/")                   // the URL it lives at
class HomeScreen : ServerComponent {

    @LiveState var count = 0                      // state lives on the server

    @LiveAction fun increment() { count++ }       // a thing the user can do

    override fun render(): UiNode =
        div(cls("card"),
            h1("Hello SpringReact 👋"),
            button(onClick("increment"), "Count: $count"))
}
```

## Step 3 — run

```bash
./gradlew bootRun
```

Open <http://localhost:8080>. Click the button — the count goes up. The number lives in
your Kotlin object on the server; the click ran `increment()`; SpringReact sent the change
back and React updated the screen.

## What just happened?

| You wrote | It means |
|---|---|
| `@LiveComponent("Home")` | "This class is a screen named Home." |
| `@Route("/")` | "Serve it at `/`." No controller needed. |
| `@LiveState var count` | "`count` is UI state — keep it on the server, send it to the browser." |
| `@LiveAction fun increment()` | "The browser can call this." |
| `render()` | "Here's what the screen looks like, built in Kotlin." |

## TODO checklist

- [ ] Add the dependency
- [ ] Create one `@LiveComponent` `@Route("/")` class
- [ ] `./gradlew bootRun` and open `/`
- [ ] Next: [Server Components](02-server-components.md)

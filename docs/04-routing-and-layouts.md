# 4. Routing & Layouts

## Give a screen a URL

Add `@Route` to a component — no controller needed:

```kotlin
@LiveComponent("Home")
@Route("/")
class HomeScreen : ServerComponent { ... }

@LiveComponent("Users")
@Route("/users")
class UsersScreen : ServerComponent { ... }
```

Now `/` and `/users` each serve their screen. Deep links and refresh work — the server
renders the right screen for the URL.

## Navigation (no full reload)

Just use normal links:

```kotlin
a(href("/users"), "Go to users")
```

If the target is one of your `@Route`s, SpringReact navigates **client-side** over the
WebSocket — instant, no white flash. External links (`https://…`) behave normally.

Navigate from code (client side, e.g. inside a custom widget):

```js
window.SpringReact.navigate("/users")
```

## Layouts (shared chrome)

A layout is just a component with a `slot()` where the current screen goes:

```kotlin
@LiveComponent("Main")
class MainLayout : ServerComponent {
    override fun render(): UiNode =
        div(cls("app"),
            header(cls("nav"), a(href("/"), "Home"), a(href("/users"), "Users")),
            main(cls("content"), slot()))   // <-- the active screen renders here
}
```

Attach a layout to screens with `layout = "..."`:

```kotlin
@LiveComponent("Home")  @Route("/",      layout = "Main")  class HomeScreen  : ServerComponent { ... }
@LiveComponent("Users") @Route("/users", layout = "Main")  class UsersScreen : ServerComponent { ... }
```

Now navigating between `/` and `/users` keeps the nav bar **mounted** (it doesn't flicker
or reset); only the inner screen swaps. Layouts can have their own `@LiveState`/`@LiveAction`
too (e.g. a sidebar toggle that survives navigation).

## How it fits together

```
window.__ROUTES__ = { "/": {view:"Home",layout:"Main"}, "/users": {view:"Users",layout:"Main"} }
        │
Router (in the bundled runtime) watches the URL + link clicks
        │
renders   <ServerView name="Main" slot={ <ServerView name="Home"/> } />
            └ layout stays mounted        └ inner screen, keyed by view → remounts on nav
```

## Gotchas

- The layout name must match a `@LiveComponent` name.
- A path with both a `@Route` and a Spring `@Controller` mapping will conflict — pick one.
- Controller-rendered views still work (return a view name from an MVC controller); they
  just won't be in the client route table unless they also have `@Route`.

## TODO checklist

- [ ] Add `@Route` to two screens
- [ ] Create a `Main` layout with `slot()`
- [ ] Set `layout = "Main"` on both screens
- [ ] Click between them — the nav bar stays put
- [ ] Next: [Forms & Validation](05-forms-and-validation.md)

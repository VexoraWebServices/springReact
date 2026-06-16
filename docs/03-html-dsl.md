# 3. The HTML DSL

You build UI by calling functions from `io.springreact.jsc.Html`. Import them all:

```kotlin
import io.springreact.jsc.Html.*
```

Each function returns a `UiNode`. Nest them to build a tree.

## Elements

```kotlin
div(
  h1("Title"),
  p("Some text"),
  ul(
    li("one"),
    li("two"),
  ),
)
```

Available: `div, span, section, header, footer, nav, main, h1, h2, h3, p, strong, em,
code, small, a, button, ul, li, label, input, form, br`. Need another tag? Use `el`:

```kotlin
el("article", h1("hi"))
```

## Text

Plain strings become text automatically. `"Count: $count"` just works. Or be explicit
with `text(value)`.

## Attributes

Pass attribute helpers anywhere in an element's arguments:

```kotlin
div(cls("card"),                       // class="card"
    id("main"),
    a(href("/about"), "About"),
    input(type("text"), placeholder("Name"), value(name)))
```

Helpers: `cls, id, href, type, placeholder, value, checked, disabled, name, key, attr`.
For anything else: `attr("data-x", "y")`.

## Events

Events call your `@LiveAction`s by name:

```kotlin
button(onClick("increment"), "+")                 // calls increment()
button(onClick("add", 10), "+10")                 // calls add(10)
input(value(name), onChangeValue("setName"))      // calls setName(<input value>)
```

- `onClick(action, ...args)` — fixed arguments
- `onChangeValue(action)` — passes the input's current value
- `onChange(action, ...args)` — change event, fixed args
- `onSubmit(action)` — form submit; see [Forms](05-forms-and-validation.md)

## Lists

`map` over your data; lists are spliced in automatically:

```kotlin
ul(items.map { li(key(it.id), it.text) })
```

Add `key(...)` for efficient list updates — see [Lists & Keys](09-lists-and-keys.md).

## Conditionals

Just use normal Kotlin:

```kotlin
div(
  if (error.isNotEmpty()) p(cls("error"), error) else span(),
  if (loading) span("Loading…") else button(onClick("load"), "Load"),
)
```

## Full example

```kotlin
override fun render(): UiNode =
    div(cls("card"),
        h1("Profile"),
        label("Name ", input(type("text"), value(name), onChangeValue("setName"))),
        if (name.isBlank()) p(cls("hint"), "Type your name") else p("Hello, $name!"),
        button(onClick("save"), "Save"))
```

## TODO checklist

- [ ] Build a small tree with `div`/`h1`/`button`
- [ ] Wire a button to an action with `onClick`
- [ ] Render a list with `map` + `key`
- [ ] Next: [Routing & Layouts](04-routing-and-layouts.md)

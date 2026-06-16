# 11. Tutorial: Build a Todo App

This ties everything together: a layout, two routes, a validated form, a keyed list, and a
live "X todos" badge that updates for everyone. ~40 lines of Kotlin, no frontend files.

## 1. The project

`build.gradle.kts` — just add the dependency (and the Spring Boot plugin you already use):

```kotlin
dependencies {
    implementation("io.springreact:SpringReact:0.1.0")
}
```

A normal Spring Boot main class:

```kotlin
@SpringBootApplication
class App
fun main(args: Array<String>) { runApplication<App>(*args) }
```

## 2. A place to store todos (a service)

```kotlin
@Service
class TodoStore {
    data class Todo(val id: Int, val text: String, var done: Boolean = false)
    private val items = mutableListOf<Todo>()
    private val seq = AtomicInteger(0)

    fun all(): List<Todo> = items.toList()
    fun add(text: String) { items.add(Todo(seq.incrementAndGet(), text)) }
    fun toggle(id: Int) { items.find { it.id == id }?.let { it.done = !it.done } }
    fun remove(id: Int) { items.removeIf { it.id == id } }
    fun count() = items.size
}
```

## 3. A layout with a live count badge

```kotlin
@LiveComponent("Main")
class MainLayout(private val store: TodoStore) : ServerComponent {
    override fun render(): UiNode =
        div(cls("app"),
            header(cls("nav"),
                a(href("/"), "Todos"),
                a(href("/about"), "About"),
                span(cls("badge"), "${store.count()} items")),
            main(slot()))
}
```

## 4. The form DTO

```kotlin
data class NewTodo(
    @field:NotBlank(message = "Please type something")
    val text: String = "",
)
```

## 5. The Todos screen — form + keyed list + broadcast

```kotlin
@LiveComponent("Todos")
@Route("/", layout = "Main")
class TodosScreen(
    private val store: TodoStore,
    private val live: LiveBroadcaster,
) : ServerComponent {

    @LiveState var error = ""

    @LiveAction
    fun add(form: NewTodo, errors: LiveErrors) {
        if (errors.hasErrors()) { error = errors["text"] ?: "invalid"; return }
        store.add(form.text); error = ""
        live.broadcast("Main")          // refresh the count badge everywhere
    }

    @LiveAction fun toggle(id: Int) { store.toggle(id) }
    @LiveAction fun remove(id: Int)  { store.remove(id); live.broadcast("Main") }

    override fun render(): UiNode =
        div(cls("card"),
            h1("Todos"),
            form(onSubmit("add"),
                input(type("text"), name("text"), placeholder("What needs doing?")),
                button(type("submit"), "Add")),
            if (error.isNotEmpty()) p(cls("error"), error) else span(),
            ul(cls("list"), store.all().map { t ->
                li(key(t.id),
                   label(input(type("checkbox"), checked(t.done), onChange("toggle", t.id)),
                         " ${t.text}"),
                   button(cls("ghost"), onClick("remove", t.id), "✕"))
            }))
}
```

## 6. An About screen (same layout)

```kotlin
@LiveComponent("About")
@Route("/about", layout = "Main")
class AboutScreen : ServerComponent {
    override fun render(): UiNode =
        div(cls("card"), h1("About"), p("Built with SpringReact."), a(href("/"), "← Back"))
}
```

## 7. Run

```bash
./gradlew bootRun
```

Open <http://localhost:8080>:

- Add a todo → it appears (keyed list); empty text shows the validation error.
- Toggle/remove items → updates instantly.
- The "N items" badge in the header is shared — open a second tab and watch both change.
- Click **About** → the header stays put (layout), only the body swaps (client nav).

You just built a multi-page, realtime, validated app with **no REST controllers, no
frontend project, and no npm**.

## What to try next

- [ ] Add `@LiveAuthorize("ADMIN")` to `remove` and a `LiveSecurity` bean
- [ ] Add a `StarRating` custom widget to each todo (see [Custom Widgets](07-custom-widgets.md))
- [ ] Read [How It Works](12-how-it-works.md) to understand the machinery

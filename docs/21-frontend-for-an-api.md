# 21. Using SpringReact as the Frontend for a REST API

Already have (or want) a Spring Boot **REST API**? You can use SpringReact purely as the
**UI layer** in front of it. Because your screens are Kotlin running on the JVM, they can
reach your data two ways:

| | When to use | How |
|---|---|---|
| **A. Same app — call the service directly** | The API and the UI are one Spring Boot app | Inject the same `@Service`/repository the controllers use. No HTTP. |
| **B. Separate API — call it over HTTP** | The API is a different service (or you want a clean tier split) | Use Spring's `RestClient` to call the API's endpoints. |

> **Key idea:** a SpringReact screen holds the data it shows in `@LiveState`, **loads it in
> `onMount`**, and **refreshes after each action**. Keep `render()` pure — don't make
> blocking calls inside it.

---

## A. Same app — inject the service (simplest, recommended)

Your REST controller and your SpringReact screen live in the same app, so just share the
service bean. No HTTP, no serialization — the fastest option.

```kotlin
// Your existing API
@RestController @RequestMapping("/api/todos")
class TodoApiController(private val store: TodoStore) {
    @GetMapping fun list() = store.all()
    @PostMapping fun add(@RequestBody b: NewTodo) = store.add(b.text)
}

// The SpringReact UI — injects the SAME store the controller uses
@LiveComponent("Home")
@Route("/")
class TodosScreen(private val store: TodoStore) : ServerComponent {

    @LiveAction fun add(form: AddForm, errors: LiveErrors) {
        if (errors.hasErrors()) return
        store.add(form.text)          // same service the API uses
    }
    @LiveAction fun toggle(id: Int) { store.toggle(id) }

    override fun render(): UiNode =
        ul(store.all().map { li(key(it.id), it.text) })   // read straight from the store
}
```

Your `/api/todos` endpoints stay available for mobile apps and third parties; the UI just
talks to the service directly.

---

## B. Separate API — call it over HTTP with `RestClient`

When the API is a different service, call its endpoints from your screen. Wrap the calls in
a small client bean.

**1. A typed client** (base URL is configurable, so you can point it anywhere):

```kotlin
data class TodoDto(val id: Int, val text: String, val done: Boolean)
data class NewTodo(val text: String = "")

@Component
class TodoApiClient(
    @Value("\${todo.api.base-url:http://localhost:8080}") baseUrl: String,
) {
    private val rest = RestClient.create(baseUrl)
    private val listType = object : ParameterizedTypeReference<List<TodoDto>>() {}

    fun list(): List<TodoDto> = rest.get().uri("/api/todos").retrieve().body(listType) ?: emptyList()
    fun add(text: String) { rest.post().uri("/api/todos").body(NewTodo(text)).retrieve().toBodilessEntity() }
    fun toggle(id: Int) { rest.put().uri("/api/todos/{id}/toggle", id).retrieve().toBodilessEntity() }
    fun remove(id: Int) { rest.delete().uri("/api/todos/{id}", id).retrieve().toBodilessEntity() }
}
```

```properties
# application.properties — point at any API service
todo.api.base-url=https://your-api.example.com
```

**2. A screen that loads on mount and refreshes after actions:**

```kotlin
@LiveComponent("Home")
@Route("/")
class TodosScreen(private val api: TodoApiClient) : ServerComponent, LiveLifecycle {

    @LiveState var items: List<TodoDto> = emptyList()
    @LiveState var error = ""

    override fun onMount() { items = api.list() }                 // load from the API

    @LiveAction fun add(form: AddForm, errors: LiveErrors) {
        if (errors.hasErrors()) { error = errors["text"] ?: "invalid"; return }
        api.add(form.text)                                        // POST to the API
        error = ""; items = api.list()                            // refresh
    }
    @LiveAction fun toggle(id: Int) { api.toggle(id); items = api.list() }
    @LiveAction fun remove(id: Int) { api.remove(id); items = api.list() }

    override fun render(): UiNode =
        div(cls("card"),
            h1("Todos"),
            form(onSubmit("add"),
                input(type("text"), name("text"), placeholder("Add a task…")),
                button(type("submit"), "Add")),
            if (error.isNotEmpty()) p(cls("error"), error) else span(),
            ul(items.map { t ->
                li(key(t.id),
                    label(input(type("checkbox"), checked(t.done), onChange("toggle", t.id)), " ${t.text}"),
                    button(onClick("remove", t.id), "✕"))
            }))
}
```

That's it — the browser sees a live, server-rendered UI; the data comes from your REST API.

### Runnable example

[`examples/todo-api`](../examples/todo-api) is exactly this: a REST API (`/api/todos`) plus a
SpringReact UI that consumes it via `RestClient`, in one runnable app.

```bash
./gradlew publishToMavenLocal
cd examples/todo-api && gradle bootRun     # UI at /, API at /api/todos
```

## Tips

- **Load in `onMount`, not `render()`.** `render()` may run many times; keep it free of I/O.
  Store results in `@LiveState`.
- **Refresh after mutations** (or update local state optimistically) so the UI reflects the
  new server state.
- **Slow API?** Use the async pattern — set a "loading" flag, fetch on a background thread,
  then `LiveContext.current().handle().update()`. See
  [Async, Loading & Redirects](13-async-and-redirects.md).
- **Auth headers / tokens** can be added to the `RestClient` (default headers or per-request),
  and you can read the user's token via `LiveContext.current().header("Authorization")`.

## TODO checklist

- [ ] Same app? Inject the service directly (Option A)
- [ ] Separate API? Wrap it in a `RestClient` bean (Option B), base URL configurable
- [ ] Load data in `onMount`, store in `@LiveState`, refresh after actions
- [ ] Next: [Async, Loading & Redirects](13-async-and-redirects.md)

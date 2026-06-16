# 5. Forms & Validation

SpringReact binds an HTML form to a typed object and validates it with standard Bean
Validation — all on the server.

## Step 1 — a form DTO

A data class with validation annotations:

```kotlin
import jakarta.validation.constraints.NotBlank

data class TodoForm(
    @field:NotBlank(message = "title is required")
    val title: String = "",
    val done: Boolean = false,
)
```

> Tip: give fields default values so binding works even when a field is missing.

## Step 2 — a form action

The action takes the DTO plus a `LiveErrors` parameter. SpringReact validates the DTO and
fills `LiveErrors` **before** your method runs:

```kotlin
@LiveComponent("Todos")
class TodoScreen : ServerComponent {

    @LiveState var items = mutableListOf<String>()
    @LiveState var error = ""

    @LiveAction
    fun add(form: TodoForm, errors: LiveErrors) {
        if (errors.hasErrors()) {        // validation failed
            error = errors["title"] ?: "invalid"
            return
        }
        items.add(form.title)            // all good
        error = ""
    }

    override fun render(): UiNode =
        div(cls("card"),
            form(onSubmit("add"),
                input(type("text"), name("title")),    // name = DTO field
                button(type("submit"), "Add")),
            if (error.isNotEmpty()) p(cls("error"), error) else span(),
            ul(items.map { li(it) }))
}
```

## How binding works

- `onSubmit("add")` tells the client: on submit, gather the form's **named** fields into
  an object and call `add(thatObject)`.
- Each `input(name("title"))` becomes a field on the object. Checkboxes become booleans.
- The server turns that object into your `TodoForm` (typed), validates it, and calls `add`.

```
<input name="title"> + <input type="checkbox" name="done">
        │ submit
        ▼
{ "title": "Buy milk", "done": false }   →   TodoForm(title="Buy milk", done=false)
        │ Bean Validation
        ▼
errors.hasErrors() == false  →  your action runs
```

## Showing errors

Errors are just state. Put the message in a `@LiveState` field and render it (as above).
Want per-field errors? Store a `Map<String,String>`:

```kotlin
@LiveState var fieldErrors = mapOf<String, String>()

@LiveAction
fun add(form: TodoForm, errors: LiveErrors) {
    if (errors.hasErrors()) { fieldErrors = errors.asMap(); return }
    fieldErrors = emptyMap()
    items.add(form.title)
}
```

## Validation annotations you can use

Any Jakarta Bean Validation constraint: `@NotBlank`, `@NotNull`, `@Size`, `@Email`,
`@Min`, `@Max`, `@Pattern`, … (Hibernate Validator is included via
`spring-boot-starter-validation`).

## TODO checklist

- [ ] Make a DTO with `@field:NotBlank`
- [ ] An action `fun save(form: MyForm, errors: LiveErrors)`
- [ ] A `form(onSubmit("save"), input(name("...")), button(type("submit"), "Save"))`
- [ ] Render the error from state
- [ ] Next: [Realtime Broadcast](06-realtime-broadcast.md)

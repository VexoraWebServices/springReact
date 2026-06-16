package com.example.todoapi

import com.vexora.springreact.jsc.Html.button
import com.vexora.springreact.jsc.Html.checked
import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.form
import com.vexora.springreact.jsc.Html.h1
import com.vexora.springreact.jsc.Html.input
import com.vexora.springreact.jsc.Html.key
import com.vexora.springreact.jsc.Html.label
import com.vexora.springreact.jsc.Html.li
import com.vexora.springreact.jsc.Html.name
import com.vexora.springreact.jsc.Html.onChange
import com.vexora.springreact.jsc.Html.onClick
import com.vexora.springreact.jsc.Html.onSubmit
import com.vexora.springreact.jsc.Html.p
import com.vexora.springreact.jsc.Html.placeholder
import com.vexora.springreact.jsc.Html.span
import com.vexora.springreact.jsc.Html.type
import com.vexora.springreact.jsc.Html.ul
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveAction
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveErrors
import com.vexora.springreact.live.LiveLifecycle
import com.vexora.springreact.live.LiveState
import com.vexora.springreact.web.Route
import jakarta.validation.constraints.NotBlank

/** Form DTO for the add field (validated before the action runs). */
data class AddForm(
    @field:NotBlank(message = "Type something first")
    val text: String = "",
)

/**
 * A SpringReact screen used purely as the FRONTEND for the REST API. It holds the data it
 * shows in @LiveState and talks to the API through TodoApiClient (HTTP). It loads on mount
 * and refreshes after each action — render() stays pure (no blocking calls inside it).
 */
@LiveComponent("Home")
@Route("/", title = "Todos (API-backed)")
class TodosScreen(private val api: TodoApiClient) : ServerComponent, LiveLifecycle {

    @LiveState var items: List<TodoDto> = emptyList()
    @LiveState var error = ""

    /** Load the list from the API when the screen first mounts. */
    override fun onMount() {
        items = api.list()
    }

    @LiveAction
    fun add(formData: AddForm, errors: LiveErrors) {
        if (errors.hasErrors()) { error = errors["text"] ?: "invalid"; return }
        api.add(formData.text)       // POST /api/todos
        error = ""
        items = api.list()           // refresh from the API
    }

    @LiveAction
    fun toggle(id: Int) {
        api.toggle(id)               // PUT /api/todos/{id}/toggle
        items = api.list()
    }

    @LiveAction
    fun remove(id: Int) {
        api.remove(id)               // DELETE /api/todos/{id}
        items = api.list()
    }

    override fun render(): UiNode =
        div(
            cls("card"),
            h1("Todos"),
            p(cls("muted"), "Data comes from the REST API at /api/todos"),
            form(
                cls("add"),
                onSubmit("add"),
                input(type("text"), name("text"), placeholder("Add a task…")),
                button(type("submit"), cls("primary"), "Add"),
            ),
            if (error.isNotEmpty()) p(cls("error"), error) else span(),
            ul(
                cls("list"),
                items.map { t ->
                    li(
                        key(t.id),
                        cls(if (t.done) "done" else ""),
                        label(
                            input(type("checkbox"), checked(t.done), onChange("toggle", t.id)),
                            " ${t.text}",
                        ),
                        button(cls("ghost"), onClick("remove", t.id), "✕"),
                    )
                },
            ),
        )
}

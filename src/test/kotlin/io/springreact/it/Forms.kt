package io.springreact.it

import io.springreact.jsc.Html.button
import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.form
import io.springreact.jsc.Html.input
import io.springreact.jsc.Html.li
import io.springreact.jsc.Html.name
import io.springreact.jsc.Html.onSubmit
import io.springreact.jsc.Html.p
import io.springreact.jsc.Html.span
import io.springreact.jsc.Html.type
import io.springreact.jsc.Html.ul
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveAction
import io.springreact.live.LiveComponent
import io.springreact.live.LiveErrors
import io.springreact.live.LiveState
import jakarta.validation.constraints.NotBlank

/** A typed, validated form DTO bound from the submitted fields. */
data class TodoForm(
    @field:NotBlank(message = "title is required")
    val title: String = "",
    val done: Boolean = false,
)

/** A form screen: submit binds + validates a DTO; errors render from server state. */
@LiveComponent("Form")
class FormScreen : ServerComponent {

    @LiveState
    var items: MutableList<String> = mutableListOf()

    @LiveState
    var error: String = ""

    @LiveAction
    fun save(todo: TodoForm, errors: LiveErrors) {
        if (errors.hasErrors()) {
            error = errors["title"] ?: "invalid"
            return
        }
        items.add(todo.title)
        error = ""
    }

    override fun render(): UiNode =
        div(
            cls("card"),
            form(
                onSubmit("save"),
                input(type("text"), name("title")),
                button(type("submit"), "Add"),
            ),
            if (error.isNotEmpty()) p(cls("error"), error) else span(),
            ul(cls("list"), items.map { li(it) }),
        )
}

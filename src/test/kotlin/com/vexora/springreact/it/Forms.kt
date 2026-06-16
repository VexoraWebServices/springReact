package com.vexora.springreact.it

import com.vexora.springreact.jsc.Html.button
import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.form
import com.vexora.springreact.jsc.Html.input
import com.vexora.springreact.jsc.Html.li
import com.vexora.springreact.jsc.Html.name
import com.vexora.springreact.jsc.Html.onSubmit
import com.vexora.springreact.jsc.Html.p
import com.vexora.springreact.jsc.Html.span
import com.vexora.springreact.jsc.Html.type
import com.vexora.springreact.jsc.Html.ul
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveAction
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveErrors
import com.vexora.springreact.live.LiveState
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

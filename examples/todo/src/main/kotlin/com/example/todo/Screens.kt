package com.example.todo

import com.vexora.springreact.jsc.Html.a
import com.vexora.springreact.jsc.Html.button
import com.vexora.springreact.jsc.Html.checked
import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.h1
import com.vexora.springreact.jsc.Html.header
import com.vexora.springreact.jsc.Html.href
import com.vexora.springreact.jsc.Html.input
import com.vexora.springreact.jsc.Html.key
import com.vexora.springreact.jsc.Html.label
import com.vexora.springreact.jsc.Html.li
import com.vexora.springreact.jsc.Html.main
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
import com.vexora.springreact.live.LiveBroadcaster
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveErrors
import com.vexora.springreact.live.LiveState
import com.vexora.springreact.web.Route
import jakarta.validation.constraints.NotBlank

/** A validated form DTO. */
data class NewTodo(
    @field:NotBlank(message = "Please type something")
    val text: String = "",
)

/** Shared layout with a live item-count badge. */
@LiveComponent("Main")
class MainLayout(private val store: TodoStore) : ServerComponent {
    override fun render(): UiNode =
        div(
            cls("app"),
            header(
                cls("nav"),
                a(href("/"), "Todos"),
                a(href("/about"), "About"),
                span(cls("badge"), "${store.count()} items"),
            ),
            main(com.vexora.springreact.jsc.Html.slot()),
        )
}

/** The todos screen: validated form + keyed list + broadcast to refresh the badge. */
@LiveComponent("Todos")
@Route("/", layout = "Main", title = "Todos")
class TodosScreen(
    private val store: TodoStore,
    private val live: LiveBroadcaster,
) : ServerComponent {

    @LiveState
    var error = ""

    @LiveAction
    fun add(form: NewTodo, errors: LiveErrors) {
        if (errors.hasErrors()) { error = errors["text"] ?: "invalid"; return }
        store.add(form.text); error = ""
        live.broadcast("Main") // update the count badge for everyone
    }

    @LiveAction fun toggle(id: Int) { store.toggle(id) }
    @LiveAction fun remove(id: Int) { store.remove(id); live.broadcast("Main") }

    override fun render(): UiNode =
        div(
            cls("card"),
            h1("Todos"),
            com.vexora.springreact.jsc.Html.form(
                onSubmit("add"),
                input(type("text"), name("text"), placeholder("What needs doing?")),
                button(type("submit"), "Add"),
            ),
            if (error.isNotEmpty()) p(cls("error"), error) else span(),
            ul(cls("list"), store.all().map { t ->
                li(
                    key(t.id),
                    label(
                        input(type("checkbox"), checked(t.done), onChange("toggle", t.id)),
                        " ${t.text}",
                    ),
                    button(cls("ghost"), onClick("remove", t.id), "✕"),
                )
            }),
        )
}

/** A second page sharing the same layout. */
@LiveComponent("About")
@Route("/about", layout = "Main", title = "About")
class AboutScreen : ServerComponent {
    override fun render(): UiNode =
        div(cls("card"), h1("About"), p("Built with SpringReact — Kotlin server components."), a(href("/"), "← Back"))
}

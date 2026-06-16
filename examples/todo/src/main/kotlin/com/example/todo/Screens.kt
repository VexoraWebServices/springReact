package com.example.todo

import io.springreact.jsc.Html.a
import io.springreact.jsc.Html.button
import io.springreact.jsc.Html.checked
import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.h1
import io.springreact.jsc.Html.header
import io.springreact.jsc.Html.href
import io.springreact.jsc.Html.input
import io.springreact.jsc.Html.key
import io.springreact.jsc.Html.label
import io.springreact.jsc.Html.li
import io.springreact.jsc.Html.main
import io.springreact.jsc.Html.name
import io.springreact.jsc.Html.onChange
import io.springreact.jsc.Html.onClick
import io.springreact.jsc.Html.onSubmit
import io.springreact.jsc.Html.p
import io.springreact.jsc.Html.placeholder
import io.springreact.jsc.Html.span
import io.springreact.jsc.Html.type
import io.springreact.jsc.Html.ul
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveAction
import io.springreact.live.LiveBroadcaster
import io.springreact.live.LiveComponent
import io.springreact.live.LiveErrors
import io.springreact.live.LiveState
import io.springreact.web.Route
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
            main(io.springreact.jsc.Html.slot()),
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
            io.springreact.jsc.Html.form(
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

package com.example.todo

import com.vexora.springreact.jsc.Html.a
import com.vexora.springreact.jsc.Html.button
import com.vexora.springreact.jsc.Html.checked
import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.form
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
import com.vexora.springreact.live.LiveContext
import com.vexora.springreact.live.LiveErrors
import com.vexora.springreact.live.LiveState
import com.vexora.springreact.web.Route
import jakarta.validation.constraints.NotBlank

/** Validated form DTO for adding a todo. */
data class NewTodo(
    @field:NotBlank(message = "Type something first")
    val text: String = "",
)

/** Shared layout with a nav and a live item-count badge. */
@LiveComponent("Main")
class MainLayout(private val store: TodoStore) : ServerComponent {
    override fun render(): UiNode =
        div(
            cls("app"),
            header(
                cls("nav"),
                a(cls("brand"), href("/"), "✦ Tasks"),
                div(
                    cls("nav-links"),
                    a(href("/"), "Todos"),
                    a(href("/about"), "About"),
                    span(cls("badge"), "${store.count()}"),
                ),
            ),
            main(com.vexora.springreact.jsc.Html.slot()),
        )
}

/**
 * The Todos screen — a showcase of SpringReact concepts:
 * controlled-yet-clearing input, inline validation, a duplicate-name **toast** (auto-
 * dismissed from a background thread), quantity steppers (+/-), filters, keyed list,
 * conditional rendering, DI, and cross-component broadcast.
 */
@LiveComponent("Todos")
@Route("/", layout = "Main", title = "Todos · SpringReact")
class TodosScreen(
    private val store: TodoStore,
    private val live: LiveBroadcaster,
) : ServerComponent {

    @LiveState var error = ""
    @LiveState var toast = ""
    @LiveState var tab = "all"          // all | active | done
    @LiveState var inputVersion = 0         // bump to clear the (uncontrolled) input

    @LiveAction
    fun add(todo: NewTodo, errors: LiveErrors) {
        if (errors.hasErrors()) { error = errors["text"] ?: "invalid"; return }
        val text = todo.text.trim()
        if (store.exists(text)) {
            showToast("\"$text\" is already on the list")
            return
        }
        store.add(text)
        error = ""
        inputVersion++                      // remount the input → it clears
        live.broadcast("Main")              // refresh the count badge everywhere
    }

    @LiveAction fun toggle(id: Int) { store.toggle(id) }
    @LiveAction fun inc(id: Int) { store.setQty(id, +1) }
    @LiveAction fun dec(id: Int) { store.setQty(id, -1) }
    @LiveAction fun remove(id: Int) { store.remove(id); live.broadcast("Main") }
    @LiveAction fun setFilter(value: String) { tab = value }
    @LiveAction fun clearDone() { store.clearDone(); live.broadcast("Main") }
    @LiveAction fun dismissToast() { toast = "" }

    /** Show a toast and clear it ~2.5s later from a background thread (async self-update). */
    private fun showToast(message: String) {
        val handle = LiveContext.current().handle()
        toast = message
        Thread {
            Thread.sleep(2500)
            toast = ""
            handle.update()
        }.apply { isDaemon = true }.start()
    }

    private fun visible(): List<TodoStore.Todo> = when (tab) {
        "active" -> store.all().filter { !it.done }
        "done" -> store.all().filter { it.done }
        else -> store.all()
    }

    override fun render(): UiNode {
        val items = visible()
        return div(
            cls("card"),
            h1("Todos"),

            // Add form — uncontrolled input; key bump clears it after a successful add.
            form(
                cls("add"),
                onSubmit("add"),
                input(
                    key("draft-$inputVersion"),
                    type("text"),
                    name("text"),
                    placeholder("Add a task and press Enter…"),
                ),
                button(type("submit"), cls("primary"), "Add"),
            ),
            if (error.isNotEmpty()) p(cls("error"), error) else span(),

            // Filter chips.
            div(
                cls("filters"),
                chip("all", "All", store.count()),
                chip("active", "Active", store.activeCount()),
                chip("done", "Done", store.count() - store.activeCount()),
            ),

            // List or empty state.
            if (items.isEmpty()) {
                div(cls("empty"), span(cls("empty-emoji"), "🗒️"), p(emptyMessage()))
            } else {
                ul(cls("list"), items.map { row(it) })
            },

            // Footer.
            div(
                cls("foot"),
                span(cls("muted"), "${store.activeCount()} left"),
                button(cls("link"), onClick("clearDone"), "Clear completed"),
            ),

            // Toast (duplicate name). Rendered only when present.
            if (toast.isNotEmpty()) {
                div(
                    cls("toast"),
                    span(cls("toast-dot"), ""),
                    span(toast),
                    button(cls("toast-x"), onClick("dismissToast"), "✕"),
                )
            } else {
                span()
            },
        )
    }

    private fun chip(value: String, text: String, count: Int): UiNode =
        button(
            cls(if (tab == value) "chip on" else "chip"),
            onClick("setFilter", value),
            "$text ($count)",
        )

    private fun row(t: TodoStore.Todo): UiNode =
        li(
            key(t.id),
            cls(if (t.done) "done" else ""),
            label(
                cls("check"),
                input(type("checkbox"), checked(t.done), onChange("toggle", t.id)),
                span(t.text),
            ),
            div(
                cls("stepper"),
                button(cls("step"), onClick("dec", t.id), "−"),
                span(cls("qty"), t.qty.toString()),
                button(cls("step"), onClick("inc", t.id), "+"),
            ),
            button(cls("ghost"), onClick("remove", t.id), "✕"),
        )

    private fun emptyMessage(): String = when (tab) {
        "active" -> "Nothing active — nice work!"
        "done" -> "No completed tasks yet."
        else -> "No tasks yet. Add your first one above."
    }
}

/** A second page sharing the same layout. */
@LiveComponent("About")
@Route("/about", layout = "Main", title = "About · SpringReact")
class AboutScreen : ServerComponent {
    override fun render(): UiNode =
        div(
            cls("card"),
            h1("About"),
            p("A SpringReact demo: server-driven React UI in Kotlin, over one WebSocket."),
            p(cls("muted"), "Try: add a duplicate name for a toast, use +/- to set quantity, filter, and toggle."),
            a(href("/"), "← Back to todos"),
        )
}

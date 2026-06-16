package io.springreact.it

import io.springreact.jsc.Html.button
import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.onClick
import io.springreact.jsc.Html.span
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveAction
import io.springreact.live.LiveComponent
import io.springreact.live.LiveContext
import io.springreact.live.LiveState

/** Server-initiated redirect: an action tells the client to navigate elsewhere. */
@LiveComponent("Redirector")
class RedirectorScreen : ServerComponent {
    @LiveAction
    fun go() {
        LiveContext.current().redirect("/users")
    }

    override fun render(): UiNode = div(button(onClick("go"), "Go"))
}

/** Async/loading: an action kicks off background work and pushes an update when done. */
@LiveComponent("Async")
class AsyncScreen : ServerComponent {

    @LiveState
    var status = "idle"

    @LiveAction
    fun load() {
        val handle = LiveContext.current().handle()   // capture for later
        status = "loading"
        Thread {
            Thread.sleep(50)                           // pretend to fetch
            status = "loaded"
            handle.update()                            // re-render from the background thread
        }.start()
    }

    override fun render(): UiNode = div(cls("card"), span("Status: $status"))
}

package com.vexora.springreact.it

import com.vexora.springreact.jsc.Html.button
import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.onClick
import com.vexora.springreact.jsc.Html.span
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveAction
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveContext
import com.vexora.springreact.live.LiveState

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

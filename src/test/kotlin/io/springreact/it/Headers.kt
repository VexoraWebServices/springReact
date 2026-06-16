package io.springreact.it

import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.span
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveAction
import io.springreact.live.LiveComponent
import io.springreact.live.LiveContext
import io.springreact.live.LiveState

/** Reads request headers/cookies from the handshake inside an action (like Next.js cookies()/headers()). */
@LiveComponent("Headers")
class HeadersScreen : ServerComponent {

    @LiveState var header = ""
    @LiveState var cookie = ""

    @LiveAction
    fun read() {
        val ctx = LiveContext.current()
        header = ctx.header("X-Test") ?: ""
        cookie = ctx.cookie("sid") ?: ""
    }

    override fun render(): UiNode =
        div(cls("card"), span("H: $header"), span("C: $cookie"))
}

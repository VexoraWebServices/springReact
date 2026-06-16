package com.vexora.springreact.it

import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.span
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveAction
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveContext
import com.vexora.springreact.live.LiveState

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

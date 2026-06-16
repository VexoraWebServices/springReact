package com.vexora.springreact.it

import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.h1
import com.vexora.springreact.jsc.Html.p
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveParam

/** A screen whose render() blows up — used to exercise the error boundary. */
@LiveComponent("Throwing")
class ThrowingScreen : ServerComponent {
    override fun render(): UiNode = throw RuntimeException("boom")
}

/** A custom error screen; receives the failure message as a route/param-style field. */
@LiveComponent("ErrorView")
class ErrorScreen : ServerComponent {
    @LiveParam
    var message: String = ""

    override fun render(): UiNode =
        div(cls("err"), h1("Oops"), p(message))
}

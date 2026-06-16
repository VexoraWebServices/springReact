package io.springreact.it

import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.h1
import io.springreact.jsc.Html.p
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveComponent
import io.springreact.live.LiveParam

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

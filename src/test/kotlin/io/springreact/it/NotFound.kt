package io.springreact.it

import io.springreact.jsc.Html.a
import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.h1
import io.springreact.jsc.Html.href
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveComponent

/** Shown for unknown URLs when spring.react.not-found-view=NotFound. */
@LiveComponent("NotFound")
class NotFoundScreen : ServerComponent {
    override fun render(): UiNode =
        div(cls("card"), h1("404 — Page not found"), a(href("/"), "Go home"))
}

package com.vexora.springreact.it

import com.vexora.springreact.jsc.Html.a
import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.h1
import com.vexora.springreact.jsc.Html.href
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveComponent

/** Shown for unknown URLs when spring.react.not-found-view=NotFound. */
@LiveComponent("NotFound")
class NotFoundScreen : ServerComponent {
    override fun render(): UiNode =
        div(cls("card"), h1("404 — Page not found"), a(href("/"), "Go home"))
}

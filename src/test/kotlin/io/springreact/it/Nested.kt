package io.springreact.it

import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.h1
import io.springreact.jsc.Html.slot
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveComponent
import io.springreact.web.Layout
import io.springreact.web.Route

/** Top-level layout. */
@LiveComponent("Root")
class RootLayout : ServerComponent {
    override fun render(): UiNode = div(cls("root"), slot())
}

/** A layout nested inside Root. */
@LiveComponent("Section")
@Layout(parent = "Root")
class SectionLayout : ServerComponent {
    override fun render(): UiNode = div(cls("section"), slot())
}

/** A page using the nested Section layout (so its chrome is Root > Section). */
@LiveComponent("Dash")
@Route("/dash", layout = "Section")
class DashScreen : ServerComponent {
    override fun render(): UiNode = div(cls("card"), h1("Dashboard"))
}

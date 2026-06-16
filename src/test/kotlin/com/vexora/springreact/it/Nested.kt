package com.vexora.springreact.it

import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.h1
import com.vexora.springreact.jsc.Html.slot
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.web.Layout
import com.vexora.springreact.web.Route

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

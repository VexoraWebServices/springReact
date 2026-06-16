package com.vexora.springreact.it

import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.h1
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveParam
import com.vexora.springreact.web.Route

/** A dynamic route: the {id} segment of the URL is bound to a @LiveParam field on mount. */
@LiveComponent("User")
@Route("/users/{id}", layout = "Main", title = "User Detail", description = "A single user")
class UserScreen : ServerComponent {

    @LiveParam
    var id: Int = 0

    override fun render(): UiNode =
        div(cls("card"), h1("User #$id"))
}

package io.springreact.it

import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.h1
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveComponent
import io.springreact.live.LiveParam
import io.springreact.web.Route

/** A dynamic route: the {id} segment of the URL is bound to a @LiveParam field on mount. */
@LiveComponent("User")
@Route("/users/{id}", layout = "Main", title = "User Detail")
class UserScreen : ServerComponent {

    @LiveParam
    var id: Int = 0

    override fun render(): UiNode =
        div(cls("card"), h1("User #$id"))
}

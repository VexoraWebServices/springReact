package io.springreact.it

import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.h1
import io.springreact.jsc.Html.input
import io.springreact.jsc.Html.onChangeValue
import io.springreact.jsc.Html.placeholder
import io.springreact.jsc.Html.type
import io.springreact.jsc.Html.value
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveAction
import io.springreact.live.LiveComponent
import io.springreact.live.LiveState

/** The same plugin, authored in Kotlin. `@field:LiveState` puts the marker on the backing field. */
@LiveComponent("KotlinGreet")
class KotlinGreetComponent : ServerComponent {

    @field:LiveState
    var who: String = "world"

    @LiveAction
    fun setName(value: String) {
        who = value
    }

    override fun render(): UiNode =
        div(
            cls("card"),
            h1("Hi, $who"),
            input(type("text"), placeholder("name"), value(who), onChangeValue("setName")),
        )
}

package com.vexora.springreact.it

import com.vexora.springreact.jsc.Html.a
import com.vexora.springreact.jsc.Html.attr
import com.vexora.springreact.jsc.Html.button
import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.h1
import com.vexora.springreact.jsc.Html.header
import com.vexora.springreact.jsc.Html.href
import com.vexora.springreact.jsc.Html.input
import com.vexora.springreact.jsc.Html.li
import com.vexora.springreact.jsc.Html.main
import com.vexora.springreact.jsc.Html.onChangeValue
import com.vexora.springreact.jsc.Html.onClick
import com.vexora.springreact.jsc.Html.p
import com.vexora.springreact.jsc.Html.placeholder
import com.vexora.springreact.jsc.Html.slot
import com.vexora.springreact.jsc.Html.span
import com.vexora.springreact.jsc.Html.type
import com.vexora.springreact.jsc.Html.ul
import com.vexora.springreact.jsc.Html.value
import com.vexora.springreact.jsc.Html.widget
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveAction
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveState
import com.vexora.springreact.web.Route

/** A persistent layout with a nav bar; child screens render into its slot. */
@LiveComponent("Main")
class MainLayout : ServerComponent {
    override fun render(): UiNode =
        div(
            cls("app"),
            header(cls("nav"), a(href("/"), "Home"), a(href("/users"), "Users")),
            main(cls("content"), slot()),
        )
}

/** Home screen: DI, state, a custom widget, and a route. */
@LiveComponent("Home")
@Route("/", layout = "Main")
class HomeScreen(private val greetings: GreetingService) : ServerComponent {

    @LiveState
    var count: Int = 0

    @LiveState
    var rating: Int = 0

    @LiveAction
    fun increment() {
        count++
    }

    @LiveAction
    fun rate(stars: Int) {
        rating = stars
    }

    override fun render(): UiNode =
        div(
            cls("card"),
            h1(greetings.hello("Home")),
            span(cls("count"), "Count: $count"),
            button(onClick("increment"), "+"),
            widget("StarRating", attr("value", rating), attr("action", "rate")),
            p(a(href("/users"), "Users")),
        )
}

/** A second routed screen. */
@LiveComponent("Users")
@Route("/users", layout = "Main")
class UsersScreen : ServerComponent {
    override fun render(): UiNode =
        div(
            cls("card"),
            h1("Users"),
            ul(cls("list"), listOf("Ada", "Linus", "Grace").map { li(it) }),
        )
}

/** A Kotlin component with an input — used to verify the live transport directly. */
@LiveComponent("KotlinGreet")
class KotlinGreetComponent : ServerComponent {

    @LiveState
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

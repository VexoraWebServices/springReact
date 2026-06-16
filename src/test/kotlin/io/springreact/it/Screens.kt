package io.springreact.it

import io.springreact.jsc.Html.a
import io.springreact.jsc.Html.attr
import io.springreact.jsc.Html.button
import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.h1
import io.springreact.jsc.Html.header
import io.springreact.jsc.Html.href
import io.springreact.jsc.Html.input
import io.springreact.jsc.Html.li
import io.springreact.jsc.Html.main
import io.springreact.jsc.Html.onChangeValue
import io.springreact.jsc.Html.onClick
import io.springreact.jsc.Html.p
import io.springreact.jsc.Html.placeholder
import io.springreact.jsc.Html.slot
import io.springreact.jsc.Html.span
import io.springreact.jsc.Html.type
import io.springreact.jsc.Html.ul
import io.springreact.jsc.Html.value
import io.springreact.jsc.Html.widget
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveAction
import io.springreact.live.LiveComponent
import io.springreact.live.LiveState
import io.springreact.web.Route

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

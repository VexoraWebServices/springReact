package com.example.minimal

import io.springreact.jsc.Html.button
import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.h1
import io.springreact.jsc.Html.onClick
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveAction
import io.springreact.live.LiveComponent
import io.springreact.live.LiveState
import io.springreact.web.Route
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MinimalApp

fun main(args: Array<String>) {
    runApplication<MinimalApp>(*args)
}

@LiveComponent("Home")
@Route("/")
class HomeScreen : ServerComponent {
    @LiveState var count = 0
    @LiveAction fun inc() { count++ }

    override fun render(): UiNode =
        div(cls("card"), h1("Hello from the SpringReact plugin!"), button(onClick("inc"), "Count: $count"))
}

package com.example.minimal

import com.vexora.springreact.jsc.Html.button
import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.h1
import com.vexora.springreact.jsc.Html.onClick
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveAction
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveState
import com.vexora.springreact.web.Route
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

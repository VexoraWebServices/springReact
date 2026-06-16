package io.springreact.it

import io.springreact.jsc.Html.button
import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.onClick
import io.springreact.jsc.Html.span
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveAction
import io.springreact.live.LiveActionContext
import io.springreact.live.LiveComponent
import io.springreact.live.LiveInterceptor
import io.springreact.live.LiveState
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentLinkedQueue

/** Middleware: records every action and blocks any action named "forbidden". */
@Component
class RecordingInterceptor : LiveInterceptor {
    val seen = ConcurrentLinkedQueue<String>()
    override fun beforeAction(ctx: LiveActionContext): Boolean {
        seen.add("${ctx.component}#${ctx.action}")
        return ctx.action != "forbidden"
    }
}

@LiveComponent("Guarded2")
class Guarded2Screen : ServerComponent {
    @LiveState var value = 0
    @LiveAction fun ok() { value++ }
    @LiveAction fun forbidden() { value += 100 }
    override fun render(): UiNode =
        div(cls("card"), span("Value: $value"),
            button(onClick("ok"), "ok"), button(onClick("forbidden"), "forbidden"))
}

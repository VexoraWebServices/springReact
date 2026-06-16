package com.vexora.springreact.it

import com.vexora.springreact.jsc.Html.button
import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.onClick
import com.vexora.springreact.jsc.Html.span
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveAction
import com.vexora.springreact.live.LiveActionContext
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveInterceptor
import com.vexora.springreact.live.LiveState
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

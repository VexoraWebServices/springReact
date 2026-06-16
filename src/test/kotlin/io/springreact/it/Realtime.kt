package io.springreact.it

import io.springreact.jsc.Html.button
import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.onClick
import io.springreact.jsc.Html.span
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveAction
import io.springreact.live.LiveBroadcaster
import io.springreact.live.LiveComponent
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

/** Shared server state, the same for every connected client. */
@Service
class SharedCounter {
    private val value = AtomicInteger(0)
    fun get() = value.get()
    fun increment() = value.incrementAndGet()
}

/**
 * A realtime screen: bumping the shared counter broadcasts a re-render to every mounted
 * client, so all browsers update together (live dashboard / presence pattern).
 */
@LiveComponent("Dashboard")
class DashboardScreen(
    private val counter: SharedCounter,
    private val broadcaster: LiveBroadcaster,
) : ServerComponent {

    @LiveAction
    fun bump() {
        counter.increment()
        broadcaster.broadcast("Dashboard")
    }

    override fun render(): UiNode =
        div(
            cls("card"),
            span(cls("value"), "Value: ${counter.get()}"),
            button(onClick("bump"), "Bump (everyone sees it)"),
        )
}

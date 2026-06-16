package io.springreact.it

import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.span
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveComponent
import io.springreact.live.LiveLifecycle
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

/** Counts mounts/unmounts — a stand-in for a presence tracker. */
@Service
class PresenceTracker {
    val joined = AtomicInteger(0)
    val left = AtomicInteger(0)
}

@LiveComponent("Presence")
class PresenceScreen(private val tracker: PresenceTracker) : ServerComponent, LiveLifecycle {
    override fun onMount() { tracker.joined.incrementAndGet() }
    override fun onUnmount() { tracker.left.incrementAndGet() }
    override fun render(): UiNode = div(cls("card"), span("online"))
}

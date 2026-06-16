package com.vexora.springreact.it

import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.span
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveLifecycle
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

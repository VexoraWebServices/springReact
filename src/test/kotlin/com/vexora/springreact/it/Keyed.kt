package com.vexora.springreact.it

import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.key
import com.vexora.springreact.jsc.Html.li
import com.vexora.springreact.jsc.Html.ul
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveAction
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveState

/** A keyed list — reorders/removals reconcile by key, not by index. */
@LiveComponent("Keyed")
class KeyedScreen : ServerComponent {

    @LiveState
    var items: MutableList<Int> = mutableListOf(1, 2, 3)

    @LiveAction
    fun removeFirst() {
        if (items.isNotEmpty()) items.removeAt(0)
    }

    @LiveAction
    fun reverse() {
        items.reverse()
    }

    override fun render(): UiNode =
        ul(cls("list"), items.map { li(key(it), "Item $it") })
}

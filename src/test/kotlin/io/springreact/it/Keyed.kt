package io.springreact.it

import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.key
import io.springreact.jsc.Html.li
import io.springreact.jsc.Html.ul
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveAction
import io.springreact.live.LiveComponent
import io.springreact.live.LiveState

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

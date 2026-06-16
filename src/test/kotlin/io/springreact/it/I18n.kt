package io.springreact.it

import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.span
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveComponent
import io.springreact.live.LiveContext
import io.springreact.live.LiveLifecycle
import org.springframework.context.MessageSource
import java.util.Locale

/** Localised screen: captures the client's locale on mount and resolves messages with it. */
@LiveComponent("I18n")
class I18nScreen(private val messages: MessageSource) : ServerComponent, LiveLifecycle {

    private var locale: Locale = Locale.ENGLISH

    override fun onMount() {
        locale = LiveContext.current().locale()
    }

    override fun render(): UiNode =
        div(cls("card"), span(messages.getMessage("greeting", null, locale)))
}

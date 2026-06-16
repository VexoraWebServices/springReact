package com.vexora.springreact.it

import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.span
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveContext
import com.vexora.springreact.live.LiveLifecycle
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

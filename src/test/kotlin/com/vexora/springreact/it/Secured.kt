package com.vexora.springreact.it

import com.vexora.springreact.jsc.Html.button
import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.onClick
import com.vexora.springreact.jsc.Html.span
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveAction
import com.vexora.springreact.live.LiveAuthorize
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveSecurity
import com.vexora.springreact.live.LiveState
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** Test security: allow actions requiring EDITOR, deny everything else that needs a role. */
@Configuration
class TestSecurityConfig {
    @Bean
    fun liveSecurity(): LiveSecurity = LiveSecurity { _, roles ->
        roles.isEmpty() || roles.contains("EDITOR")
    }
}

@LiveComponent("Secured")
class SecuredScreen : ServerComponent {

    @LiveState
    var value: Int = 0

    @LiveAction
    @LiveAuthorize("EDITOR")
    fun allowed() {
        value++
    }

    @LiveAction
    @LiveAuthorize("ADMIN")
    fun denied() {
        value += 100
    }

    override fun render(): UiNode =
        div(
            cls("card"),
            span(cls("value"), "Value: $value"),
            button(onClick("allowed"), "allowed"),
            button(onClick("denied"), "denied"),
        )
}

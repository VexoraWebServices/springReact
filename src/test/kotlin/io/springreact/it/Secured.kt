package io.springreact.it

import io.springreact.jsc.Html.button
import io.springreact.jsc.Html.cls
import io.springreact.jsc.Html.div
import io.springreact.jsc.Html.onClick
import io.springreact.jsc.Html.span
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.live.LiveAction
import io.springreact.live.LiveAuthorize
import io.springreact.live.LiveComponent
import io.springreact.live.LiveSecurity
import io.springreact.live.LiveState
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

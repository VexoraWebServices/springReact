package com.vexora.springreact.live

import com.fasterxml.jackson.databind.ObjectMapper
import com.vexora.springreact.autoconfigure.ReactAutoConfiguration
import com.vexora.springreact.autoconfigure.ReactProperties
import jakarta.validation.Validator
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

/** Registers the single `/live` WebSocket endpoint that all components share. */
@Configuration(proxyBeanMethods = false)
@EnableWebSocket
class LiveWebSocketConfiguration(
    private val handler: LiveWebSocketHandler,
    private val properties: ReactProperties,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(handler, "/live")
            .setAllowedOriginPatterns(*properties.allowedOrigins.toTypedArray())
    }
}

/**
 * Auto-configures the live engine: component discovery, the WebSocket handler and the
 * `/live` endpoint. [LiveWebSocketConfiguration] is `@Import`ed (not a `@Bean`) so its
 * `@EnableWebSocket` is processed and the endpoint actually registers.
 */
@AutoConfiguration(after = [ReactAutoConfiguration::class])
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(WebSocketHandler::class)
@Import(LiveWebSocketConfiguration::class)
class LiveAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun liveComponentRegistry(context: ApplicationContext) = LiveComponentRegistry(context)

    /**
     * Default authorization: actions with no roles are allowed; actions that DO require
     * roles need an authenticated principal. Override this bean to do real role checks
     * (e.g. via Spring Security).
     */
    @Bean
    @ConditionalOnMissingBean
    fun liveSecurity(): LiveSecurity = LiveSecurity { principal, roles ->
        roles.isEmpty() || principal != null
    }

    @Bean
    @ConditionalOnMissingBean
    fun liveWebSocketHandler(
        registry: LiveComponentRegistry,
        objectMapper: ObjectMapper,
        validators: ObjectProvider<Validator>,
        security: LiveSecurity,
        interceptors: ObjectProvider<LiveInterceptor>,
        properties: ReactProperties,
    ) = LiveWebSocketHandler(
        registry,
        objectMapper,
        validators.ifAvailable,
        security,
        interceptors.orderedStream().toList(),
        properties.errorView,
    )

    /** The handler is the broadcaster; expose it so services/components can inject it. */
    @Bean
    @ConditionalOnMissingBean
    fun liveBroadcaster(handler: LiveWebSocketHandler): LiveBroadcaster = handler
}

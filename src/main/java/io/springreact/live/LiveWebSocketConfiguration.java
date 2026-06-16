package io.springreact.live;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Registers the single {@code /live} WebSocket endpoint that all live components share.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSocket
public class LiveWebSocketConfiguration implements WebSocketConfigurer {

    private final LiveWebSocketHandler handler;

    public LiveWebSocketConfiguration(LiveWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/live").setAllowedOriginPatterns("*");
    }
}

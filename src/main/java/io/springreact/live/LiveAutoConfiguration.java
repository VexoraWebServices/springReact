package io.springreact.live;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.springreact.autoconfigure.ReactAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.socket.WebSocketHandler;

/**
 * Auto-configures the Spring Live engine: component discovery, the WebSocket handler and
 * the {@code /live} endpoint. Active in any servlet web app that has Spring WebSocket on
 * the classpath (pulled in transitively by this starter).
 *
 * <p>{@link LiveWebSocketConfiguration} is {@code @Import}ed (not a {@code @Bean}) so its
 * class-level {@code @EnableWebSocket} is actually processed and the endpoint registers.
 */
@AutoConfiguration(after = ReactAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(WebSocketHandler.class)
@Import(LiveWebSocketConfiguration.class)
public class LiveAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LiveComponentRegistry liveComponentRegistry(ApplicationContext context) {
        return new LiveComponentRegistry(context);
    }

    @Bean
    @ConditionalOnMissingBean
    public LiveWebSocketHandler liveWebSocketHandler(LiveComponentRegistry registry,
                                                     ObjectMapper objectMapper) {
        return new LiveWebSocketHandler(registry, objectMapper);
    }
}

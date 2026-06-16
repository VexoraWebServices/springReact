package io.springreact.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.springreact.web.ReactViewResolver;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configures the HTML-shell rendering: controllers return a view name, the
 * {@link ReactViewResolver} renders the shell, and the bundled runtime mounts the
 * matching Java/Kotlin Server Component. Active in any servlet web app with this
 * framework on the classpath — no manual setup.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ReactProperties.class)
public class ReactAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ReactRenderer reactRenderer(ReactProperties properties, ObjectMapper objectMapper) {
        return new ReactRenderer(properties, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactViewResolver reactViewResolver(ReactRenderer renderer) {
        return new ReactViewResolver(renderer);
    }
}

package io.springreact.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.springreact.web.ReactViewResolver;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that wires the React view machinery into any Spring Boot servlet
 * web app that has this starter on its classpath. No manual setup required.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ReactProperties.class)
public class ReactAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ReactManifest reactManifest(ReactProperties properties, ObjectMapper objectMapper) {
        return new ReactManifest(objectMapper, properties.getManifestLocation());
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactRenderer reactRenderer(ReactProperties properties, ReactManifest manifest,
                                       ObjectMapper objectMapper) {
        return new ReactRenderer(properties, manifest, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactViewResolver reactViewResolver(ReactRenderer renderer) {
        return new ReactViewResolver(renderer);
    }

    @Bean
    @ConditionalOnMissingBean
    public ViteDevServerManager viteDevServerManager(ReactProperties properties, ReactRenderer renderer) {
        return new ViteDevServerManager(properties, renderer);
    }
}

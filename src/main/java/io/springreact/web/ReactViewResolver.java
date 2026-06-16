package io.springreact.web;

import java.util.Locale;

import io.springreact.autoconfigure.ReactRenderer;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * Resolves any controller-returned view name into a {@link ReactView}, exactly the way
 * Thymeleaf's resolver maps a name to a template. Registered at the lowest precedence so
 * it acts as the fallback for view names that nothing else handles.
 */
public class ReactViewResolver implements ViewResolver, Ordered {

    private final ReactRenderer renderer;

    public ReactViewResolver(ReactRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public View resolveViewName(String viewName, Locale locale) {
        if (viewName == null
                || viewName.startsWith("redirect:")
                || viewName.startsWith("forward:")
                // Let Spring Boot's whitelabel/error handling own the error view.
                || viewName.equals("error")) {
            return null;
        }
        return new ReactView(viewName, renderer);
    }

    @Override
    public int getOrder() {
        // Just above absolute-lowest, so genuine resolvers (if any) still win first.
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}

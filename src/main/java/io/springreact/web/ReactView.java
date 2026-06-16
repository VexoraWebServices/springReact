package io.springreact.web;

import java.util.Map;

import io.springreact.autoconfigure.ReactRenderer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

/**
 * A Spring MVC {@link View} backed by React. Analogous to a Thymeleaf template view:
 * given the model produced by the controller, it renders the React HTML shell and
 * writes it to the response.
 */
public class ReactView implements View {

    private final String viewName;
    private final ReactRenderer renderer;

    public ReactView(String viewName, ReactRenderer renderer) {
        this.viewName = viewName;
        this.renderer = renderer;
    }

    @Override
    public String getContentType() {
        return "text/html;charset=UTF-8";
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request,
                       HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        String html = renderer.render(viewName, model);
        response.getWriter().write(html);
    }
}

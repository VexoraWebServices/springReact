package io.springreact.web

import io.springreact.autoconfigure.ReactRenderer
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.web.servlet.View
import org.springframework.web.servlet.ViewResolver
import java.util.Locale

/** A Spring MVC [View] backed by a server component — the Thymeleaf-style shell. */
class ReactView(private val viewName: String, private val renderer: ReactRenderer) : View {

    override fun getContentType() = "text/html;charset=UTF-8"

    override fun render(model: Map<String, *>?, request: HttpServletRequest, response: HttpServletResponse) {
        response.contentType = contentType
        response.writer.write(renderer.render(viewName, model ?: emptyMap<String, Any?>()))
    }
}

/** Resolves any controller-returned view name into a [ReactView] (fallback resolver). */
class ReactViewResolver(private val renderer: ReactRenderer) : ViewResolver, Ordered {

    override fun resolveViewName(viewName: String, locale: Locale): View? {
        if (viewName.startsWith("redirect:") || viewName.startsWith("forward:") || viewName == "error") return null
        return ReactView(viewName, renderer)
    }

    override fun getOrder() = Ordered.LOWEST_PRECEDENCE - 10
}

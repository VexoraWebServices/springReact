package io.springreact.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import io.springreact.web.ReactView
import io.springreact.web.ReactViewResolver
import io.springreact.web.RouteRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.RouterFunctions
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.util.HtmlUtils

/** Configuration bound from `spring.react.*`. Zero config required. */
@ConfigurationProperties(prefix = "spring.react")
class ReactProperties {
    var title: String = "SpringReact"
    var runtimePath: String = "/springreact/springreact.js"

    /**
     * Allowed origin patterns for the `/live` WebSocket. Defaults to `*` for easy local
     * dev; lock this down to your real origin(s) in production, e.g.
     * `spring.react.allowed-origins=https://app.example.com`.
     */
    var allowedOrigins: List<String> = listOf("*")

    /**
     * Component to render for unknown URLs (HTTP 404). Empty = Spring's default error page.
     * e.g. `spring.react.not-found-view=NotFound`.
     */
    var notFoundView: String = ""

    /**
     * Component to render when a screen's `render()` throws. It may take a
     * `@LiveParam var message: String`. Empty = a built-in default error UI.
     */
    var errorView: String = ""
}

/**
 * Renders the HTML shell: inlines the view name, the controller model, and the route
 * table, then loads the framework's bundled runtime, which mounts the matching component.
 */
class ReactRenderer(
    private val properties: ReactProperties,
    private val objectMapper: ObjectMapper,
    private val routes: RouteRegistry,
) {
    @JvmOverloads
    fun render(
        viewName: String,
        model: Map<String, *>,
        pageTitle: String? = null,
        description: String? = null,
    ): String {
        val viewJson = writeJson(viewName)
        val modelJson = writeJson(sanitize(model))
        val routesJson = writeJson(routes.toJson())
        val layoutsJson = writeJson(routes.layoutsToJson())
        val notFoundJson = writeJson(properties.notFoundView.ifEmpty { null })
        val title = pageTitle?.takeIf { it.isNotEmpty() } ?: properties.title
        val descriptionMeta = if (!description.isNullOrEmpty()) {
            "\n  <meta name=\"description\" content=\"${HtmlUtils.htmlEscape(description)}\" />"
        } else {
            ""
        }
        return """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1" />
              <title>${HtmlUtils.htmlEscape(title)}</title>$descriptionMeta
              <script>
                window.__VIEW__ = $viewJson;
                window.__MODEL__ = $modelJson;
                window.__ROUTES__ = $routesJson;
                window.__LAYOUTS__ = $layoutsJson;
                window.__NOTFOUND__ = $notFoundJson;
              </script>
            </head>
            <body>
              <div id="root"></div>
              <script src="${HtmlUtils.htmlEscape(properties.runtimePath)}"></script>
            </body>
            </html>
        """.trimIndent() + "\n"
    }

    private fun sanitize(model: Map<String, *>): Map<String, Any?> {
        val clean = LinkedHashMap<String, Any?>()
        for ((k, v) in model) if (!k.startsWith("org.springframework")) clean[k] = v
        return clean
    }

    private fun writeJson(value: Any?): String =
        objectMapper.writeValueAsString(value).replace("</", "<\\/")
}

/**
 * Auto-configures shell rendering and convention-based routing: `@Route` components are
 * served as HTML shells; controller-returned view names resolve via [ReactViewResolver].
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ReactProperties::class)
class ReactAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun routeRegistry(context: ApplicationContext) = RouteRegistry(context)

    @Bean
    @ConditionalOnMissingBean
    fun reactRenderer(properties: ReactProperties, objectMapper: ObjectMapper, routes: RouteRegistry) =
        ReactRenderer(properties, objectMapper, routes)

    @Bean
    @ConditionalOnMissingBean
    fun reactViewResolver(renderer: ReactRenderer) = ReactViewResolver(renderer)

    /** Serve the HTML shell for every `@Route` path (so deep links / refresh work). */
    @Bean
    fun springReactRoutes(routes: RouteRegistry, renderer: ReactRenderer): RouterFunction<ServerResponse> {
        val builder = RouterFunctions.route()
        for ((path, info) in routes.all()) {
            val view = info.view
            val title = info.title
            val description = info.description
            builder.GET(path) { _ ->
                ServerResponse.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(renderer.render(view, emptyMap<String, Any?>(), title, description))
            }
        }
        return builder.build()
    }

    /** Render the configured component for HTTP 404s (so deep links to bad URLs look nice). */
    @Bean
    fun springReactErrorViewResolver(properties: ReactProperties, renderer: ReactRenderer): ErrorViewResolver =
        ErrorViewResolver { _, status, _ ->
            if (status == HttpStatus.NOT_FOUND && properties.notFoundView.isNotEmpty()) {
                ModelAndView(ReactView(properties.notFoundView, renderer))
            } else {
                null
            }
        }
}

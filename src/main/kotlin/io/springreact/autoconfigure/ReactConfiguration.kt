package io.springreact.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import io.springreact.web.ReactViewResolver
import io.springreact.web.RouteRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.RouterFunctions
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.util.HtmlUtils

/** Configuration bound from `spring.react.*`. Zero config required. */
@ConfigurationProperties(prefix = "spring.react")
class ReactProperties {
    var title: String = "SpringReact"
    var runtimePath: String = "/springreact/springreact.js"
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
    fun render(viewName: String, model: Map<String, *>): String {
        val viewJson = writeJson(viewName)
        val modelJson = writeJson(sanitize(model))
        val routesJson = writeJson(routes.toJson())
        return """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1" />
              <title>${HtmlUtils.htmlEscape(properties.title)}</title>
              <script>
                window.__VIEW__ = $viewJson;
                window.__MODEL__ = $modelJson;
                window.__ROUTES__ = $routesJson;
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
            builder.GET(path) { _ ->
                ServerResponse.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(renderer.render(view, emptyMap<String, Any?>()))
            }
        }
        return builder.build()
    }
}

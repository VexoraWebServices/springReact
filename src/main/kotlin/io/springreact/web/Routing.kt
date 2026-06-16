package io.springreact.web

import io.springreact.live.LiveComponent
import org.springframework.context.ApplicationContext

/**
 * Maps a server component to a URL path — convention-based routing, no controller needed.
 * The framework serves the HTML shell for the path and the client can navigate to it
 * without a full page reload. Pair with `@LiveComponent` (the view name) and an optional
 * `layout`.
 *
 * ```
 * @LiveComponent("Users")
 * @Route("/users", layout = "Main")
 * class UsersScreen : ServerComponent { ... }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Route(val value: String, val layout: String = "", val title: String = "")

/**
 * Discovers all `@Route` components at startup and exposes the path → (view, layout) map,
 * both to the server (to serve shells) and to the client (injected as `window.__ROUTES__`
 * for client-side navigation).
 */
class RouteRegistry(context: ApplicationContext) {

    data class RouteInfo(val view: String, val layout: String?, val title: String?)

    private val routes = LinkedHashMap<String, RouteInfo>()

    init {
        for (beanName in context.getBeanNamesForAnnotation(Route::class.java)) {
            val route = context.findAnnotationOnBean(beanName, Route::class.java) ?: continue
            val view = context.findAnnotationOnBean(beanName, LiveComponent::class.java)?.value ?: continue
            routes[route.value] = RouteInfo(view, route.layout.ifEmpty { null }, route.title.ifEmpty { null })
        }
    }

    fun all(): Map<String, RouteInfo> = routes

    fun toJson(): Map<String, Any?> {
        val json = LinkedHashMap<String, Any?>()
        routes.forEach { (path, info) ->
            val entry = LinkedHashMap<String, Any?>()
            entry["view"] = info.view
            if (info.layout != null) entry["layout"] = info.layout
            if (info.title != null) entry["title"] = info.title
            json[path] = entry
        }
        return json
    }
}

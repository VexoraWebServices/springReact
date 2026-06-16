package com.vexora.springreact.live

import org.springframework.web.socket.WebSocketSession
import java.util.Locale

/**
 * A keepable handle to one mounted component instance. Call [update] later — e.g. from a
 * background thread after async work finishes — to re-render it and push the changes.
 */
class LiveHandle internal constructor(
    private val handler: LiveWebSocketHandler,
    private val session: WebSocketSession,
    private val id: String,
) {
    /** Re-render this instance and stream the diff. Safe to call from any thread. */
    fun update() = handler.rerender(session, id)
}

/**
 * Per-action context, available inside a `@LiveAction` via [current]. Use it to redirect
 * the client or to grab a [handle] for async/loading updates.
 *
 * ```
 * @LiveAction fun load() {
 *     val handle = LiveContext.current().handle()
 *     loading = true
 *     scope.launch { data = fetch(); loading = false; handle.update() }   // updates later
 * }
 *
 * @LiveAction fun save() { ...; LiveContext.current().redirect("/done") }
 * ```
 */
class LiveContext internal constructor(
    private val handler: LiveWebSocketHandler,
    private val session: WebSocketSession,
    val id: String,
) {
    /** Tell this client to navigate to another route (client-side). */
    fun redirect(path: String) = handler.sendNavigate(session, path)

    /** A handle you can keep and call [LiveHandle.update] on later. */
    fun handle(): LiveHandle = LiveHandle(handler, session, id)

    /** The authenticated user's name, if any. */
    fun principalName(): String? = session.principal?.name

    /** A request header from the WebSocket handshake (e.g. "Authorization", "X-Tenant"). */
    fun header(name: String): String? = session.handshakeHeaders.getFirst(name)

    /** All values of a handshake request header. */
    fun headers(name: String): List<String> = session.handshakeHeaders[name] ?: emptyList()

    /** The client's preferred locale, parsed from the `Accept-Language` handshake header. */
    fun locale(): Locale {
        val accept = header("Accept-Language") ?: return Locale.getDefault()
        val tag = accept.split(",").firstOrNull()?.split(";")?.firstOrNull()?.trim()
        return if (tag.isNullOrEmpty()) Locale.getDefault() else Locale.forLanguageTag(tag)
    }

    /** A cookie value from the handshake `Cookie` header. */
    fun cookie(name: String): String? {
        val raw = session.handshakeHeaders.getFirst("Cookie") ?: return null
        return raw.split(";")
            .map { it.trim() }
            .firstOrNull { it.startsWith("$name=") }
            ?.substringAfter("=")
    }

    companion object {
        private val holder = ThreadLocal<LiveContext?>()

        @JvmStatic
        fun current(): LiveContext =
            holder.get() ?: error("LiveContext.current() must be called inside a @LiveAction")

        internal fun bind(ctx: LiveContext) = holder.set(ctx)
        internal fun clear() = holder.remove()
    }
}

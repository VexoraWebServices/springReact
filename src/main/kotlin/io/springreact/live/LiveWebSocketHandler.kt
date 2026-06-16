package io.springreact.live

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.jsc.UiTreeDiff
import jakarta.validation.Validator
import org.slf4j.LoggerFactory
import org.springframework.util.ClassUtils
import java.security.Principal
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * Push re-renders to clients without a client request — for live dashboards, presence,
 * chat, etc. Call [broadcast] (e.g. from a service after shared state changes) and every
 * mounted instance of the component re-renders and receives a patch.
 */
interface LiveBroadcaster {
    /** Re-render every mounted instance of [component] across all sessions. */
    fun broadcast(component: String)
}

/**
 * The single WebSocket endpoint that powers all live/server components. Keeps per-session
 * mounted instances, routes `mount` / `call` / `unmount`, and tracks mounts globally so it
 * can [broadcast] server-initiated updates.
 *
 * For a [ServerComponent] the first send is the full tree (`t:"tree"`); every later send is
 * a minimal `t:"patch"` diff. Plain components send `@LiveState` as `t:"state"`.
 */
open class LiveWebSocketHandler(
    private val registry: LiveComponentRegistry,
    private val objectMapper: ObjectMapper,
    private val validator: Validator? = null,
    private val security: LiveSecurity = LiveSecurity { _, roles -> roles.isEmpty() },
) : TextWebSocketHandler(), LiveBroadcaster {

    private val log = LoggerFactory.getLogger(LiveWebSocketHandler::class.java)

    /** component name -> live mounts across all sessions (for broadcast). */
    private val mounts = ConcurrentHashMap<String, MutableSet<MountRef>>()

    private data class MountRef(val session: WebSocketSession, val id: String, val component: String)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        session.attributes[INSTANCES] = ConcurrentHashMap<String, Any>()
        session.attributes[TREES] = ConcurrentHashMap<String, UiNode>()
        session.attributes[REFS] = ConcurrentHashMap<String, MountRef>()
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        refs(session).values.forEach { mounts[it.component]?.remove(it) }
        session.attributes.remove(INSTANCES)
        session.attributes.remove(TREES)
        session.attributes.remove(REFS)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val msg = objectMapper.readTree(message.payload)
        val type = msg.path("t").asText()
        val id = msg.path("id").asText()
        val instances = instances(session)
        try {
            when (type) {
                "mount" -> {
                    val component = msg.path("c").asText()
                    val instance = registry.create(component)
                    applyParams(instance, msg.path("params"))
                    instances[id] = instance
                    val ref = MountRef(session, id, component)
                    refs(session)[id] = ref
                    mounts.computeIfAbsent(component) { ConcurrentHashMap.newKeySet() }.add(ref)
                    renderInitial(session, id, instance)
                }
                "call" -> {
                    val instance = instances[id]
                    if (instance == null) {
                        sendError(session, id, "No component mounted for id '$id'")
                        return
                    }
                    LiveContext.bind(LiveContext(this, session, id))
                    try {
                        invokeAction(instance, msg.path("action").asText(), msg.path("args"), session.principal)
                        renderUpdate(session, id, instance)
                    } finally {
                        LiveContext.clear()
                    }
                }
                "unmount" -> {
                    instances.remove(id)
                    trees(session).remove(id)
                    refs(session).remove(id)?.let { mounts[it.component]?.remove(it) }
                }
                else -> sendError(session, id, "Unknown message type: '$type'")
            }
        } catch (ex: Exception) {
            log.warn("Live message failed (type={}, id={}): {}", type, id, ex.message)
            sendError(session, id, ex.message)
        }
    }

    /** Re-render a specific instance (used by LiveHandle.update from async code). */
    internal fun rerender(session: WebSocketSession, id: String) {
        if (!session.isOpen) return
        @Suppress("UNCHECKED_CAST")
        val map = session.attributes[INSTANCES] as? MutableMap<String, Any> ?: return
        val instance = map[id] ?: return
        try {
            renderUpdate(session, id, instance)
        } catch (ex: Exception) {
            log.warn("Async update for {} failed: {}", id, ex.message)
        }
    }

    /** Ask a client to navigate (server-initiated redirect). */
    internal fun sendNavigate(session: WebSocketSession, path: String) {
        send(session, mapOf("t" to "navigate", "path" to path))
    }

    override fun broadcast(component: String) {
        val refs = mounts[component] ?: return
        for (ref in refs) {
            if (!ref.session.isOpen) continue
            try {
                val instance = instances(ref.session)[ref.id] ?: continue
                renderUpdate(ref.session, ref.id, instance)
            } catch (ex: Exception) {
                log.warn("Broadcast to {} failed: {}", ref.id, ex.message)
            }
        }
    }

    private fun renderInitial(session: WebSocketSession, id: String, instance: Any) {
        if (instance is ServerComponent) {
            val tree = instance.render()
            trees(session)[id] = tree
            send(session, mapOf("t" to "tree", "id" to id, "tree" to tree.toJson()))
        } else {
            sendState(session, id, instance)
        }
    }

    private fun renderUpdate(session: WebSocketSession, id: String, instance: Any) {
        if (instance is ServerComponent) {
            val previous = trees(session)[id]
            val current = instance.render()
            trees(session)[id] = current
            if (previous == null) {
                send(session, mapOf("t" to "tree", "id" to id, "tree" to current.toJson()))
            } else {
                send(session, mapOf("t" to "patch", "id" to id, "ops" to UiTreeDiff.diff(previous, current)))
            }
        } else {
            sendState(session, id, instance)
        }
    }

    private fun invokeAction(instance: Any, action: String, args: JsonNode?, principal: Principal?) {
        val descriptor = registry.descriptor(instance)
        val method = descriptor.action(action) ?: throw IllegalArgumentException("Unknown action '$action'")

        // Authorization: method-level @LiveAuthorize wins, else class-level.
        val authorize = method.getAnnotation(LiveAuthorize::class.java)
            ?: ClassUtils.getUserClass(instance).getAnnotation(LiveAuthorize::class.java)
        if (authorize != null && !security.authorize(principal, authorize.roles)) {
            throw SecurityException("Not authorized for action '$action'")
        }

        val paramTypes = method.parameterTypes
        val callArgs = arrayOfNulls<Any?>(paramTypes.size)
        // A LiveErrors parameter is injected (and validated against the preceding form
        // argument) rather than read from the client's args; other params map to JSON args.
        var jsonIndex = 0
        var lastArg: Any? = null
        for (i in paramTypes.indices) {
            val pt = paramTypes[i]
            if (pt == LiveErrors::class.java) {
                val errors = LiveErrors()
                lastArg?.let { validate(it, errors) }
                callArgs[i] = errors
            } else {
                val arg = if (args != null && args.has(jsonIndex)) args.get(jsonIndex) else null
                val coerced = coerce(arg, pt)
                callArgs[i] = coerced
                lastArg = coerced
                jsonIndex++
            }
        }
        method.invoke(instance, *callArgs)
    }

    private fun applyParams(instance: Any, params: JsonNode?) {
        if (params == null || params.isMissingNode || !params.isObject) return
        for ((paramName, field) in registry.descriptor(instance).paramFields()) {
            val node = params.get(paramName) ?: continue
            field.set(instance, coerce(node, field.type))
        }
    }

    private fun validate(form: Any, errors: LiveErrors) {
        val v = validator ?: return
        for (violation in v.validate(form)) {
            errors.reject(violation.propertyPath.toString(), violation.message)
        }
    }

    private fun coerce(node: JsonNode?, targetType: Class<*>): Any? {
        if (node == null || node.isNull) return defaultFor(targetType)
        return objectMapper.convertValue(node, targetType)
    }

    private fun defaultFor(type: Class<*>): Any? {
        if (!type.isPrimitive) return null
        return when (type) {
            java.lang.Boolean.TYPE -> false
            java.lang.Long.TYPE -> 0L
            java.lang.Double.TYPE -> 0.0
            java.lang.Float.TYPE -> 0.0f
            else -> 0
        }
    }

    private fun sendState(session: WebSocketSession, id: String, instance: Any) {
        val state = registry.descriptor(instance).readState(instance)
        send(session, mapOf("t" to "state", "id" to id, "state" to state))
    }

    private fun sendError(session: WebSocketSession, id: String?, messageText: String?) {
        try {
            send(session, mapOf("t" to "error", "id" to (id ?: ""), "message" to (messageText ?: "error")))
        } catch (ignored: Exception) {
            // Connection is gone; nothing useful to do.
        }
    }

    protected fun send(session: WebSocketSession, payload: Any) {
        val json = objectMapper.writeValueAsString(payload)
        synchronized(session) { session.sendMessage(TextMessage(json)) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun instances(session: WebSocketSession) =
        session.attributes[INSTANCES] as MutableMap<String, Any>

    @Suppress("UNCHECKED_CAST")
    private fun trees(session: WebSocketSession) =
        session.attributes[TREES] as MutableMap<String, UiNode>

    @Suppress("UNCHECKED_CAST")
    private fun refs(session: WebSocketSession) =
        session.attributes[REFS] as MutableMap<String, MountRef>

    companion object {
        private const val INSTANCES = "live.instances"
        private const val TREES = "live.trees"
        private const val REFS = "live.refs"
    }
}

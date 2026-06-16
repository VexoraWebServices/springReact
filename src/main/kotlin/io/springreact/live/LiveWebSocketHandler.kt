package io.springreact.live

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.springreact.jsc.ServerComponent
import io.springreact.jsc.UiNode
import io.springreact.jsc.UiTreeDiff
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * The single WebSocket endpoint that powers all live/server components. It keeps a
 * per-session map of mounted instances and routes `mount` / `call` / `unmount`.
 *
 * For a [ServerComponent] the first send is the full tree (`t:"tree"`); every later send
 * is a minimal `t:"patch"` diff. Plain components send `@LiveState` as `t:"state"`.
 */
open class LiveWebSocketHandler(
    private val registry: LiveComponentRegistry,
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(LiveWebSocketHandler::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        session.attributes[INSTANCES] = ConcurrentHashMap<String, Any>()
        session.attributes[TREES] = ConcurrentHashMap<String, UiNode>()
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        session.attributes.remove(INSTANCES)
        session.attributes.remove(TREES)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val msg = objectMapper.readTree(message.payload)
        val type = msg.path("t").asText()
        val id = msg.path("id").asText()
        val instances = instances(session)
        try {
            when (type) {
                "mount" -> {
                    val instance = registry.create(msg.path("c").asText())
                    instances[id] = instance
                    renderInitial(session, id, instance)
                }
                "call" -> {
                    val instance = instances[id]
                    if (instance == null) {
                        sendError(session, id, "No component mounted for id '$id'")
                        return
                    }
                    invokeAction(instance, msg.path("action").asText(), msg.path("args"))
                    renderUpdate(session, id, instance)
                }
                "unmount" -> {
                    instances.remove(id)
                    trees(session).remove(id)
                }
                else -> sendError(session, id, "Unknown message type: '$type'")
            }
        } catch (ex: Exception) {
            log.warn("Live message failed (type={}, id={}): {}", type, id, ex.message)
            sendError(session, id, ex.message)
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

    private fun invokeAction(instance: Any, action: String, args: JsonNode?) {
        val descriptor = registry.descriptor(instance)
        val method = descriptor.action(action) ?: throw IllegalArgumentException("Unknown action '$action'")
        val paramTypes = method.parameterTypes
        val callArgs = arrayOfNulls<Any?>(paramTypes.size)
        for (i in paramTypes.indices) {
            val arg = if (args != null && args.has(i)) args.get(i) else null
            callArgs[i] = coerce(arg, paramTypes[i])
        }
        method.invoke(instance, *callArgs)
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

    companion object {
        private const val INSTANCES = "live.instances"
        private const val TREES = "live.trees"
    }
}

package io.springreact.live;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.springreact.jsc.ServerComponent;
import io.springreact.jsc.UiNode;
import io.springreact.jsc.UiTreeDiff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * The single WebSocket endpoint that powers all live components. It keeps a per-session
 * map of mounted component instances and routes three message types:
 *
 * <ul>
 *   <li>{@code mount}   – create a component instance, return its initial view</li>
 *   <li>{@code call}    – invoke an action, return the updated view</li>
 *   <li>{@code unmount} – discard the instance</li>
 * </ul>
 *
 * <p>For a {@link ServerComponent} the "view" is a rendered UI tree: the first send is the
 * full tree ({@code t:"tree"}); every subsequent send is a minimal {@code t:"patch"} diff
 * against the last tree. Plain live components send their {@code @LiveState} as
 * {@code t:"state"}. State lives here, on the server.
 */
public class LiveWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(LiveWebSocketHandler.class);
    private static final String INSTANCES = "live.instances";
    private static final String TREES = "live.trees";

    private final LiveComponentRegistry registry;
    private final ObjectMapper objectMapper;

    public LiveWebSocketHandler(LiveComponentRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.getAttributes().put(INSTANCES, new ConcurrentHashMap<String, Object>());
        session.getAttributes().put(TREES, new ConcurrentHashMap<String, UiNode>());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        session.getAttributes().remove(INSTANCES);
        session.getAttributes().remove(TREES);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode msg = objectMapper.readTree(message.getPayload());
        String type = msg.path("t").asText();
        String id = msg.path("id").asText();
        Map<String, Object> instances = instances(session);

        try {
            switch (type) {
                case "mount" -> {
                    Object instance = registry.create(msg.path("c").asText());
                    instances.put(id, instance);
                    renderInitial(session, id, instance);
                }
                case "call" -> {
                    Object instance = instances.get(id);
                    if (instance == null) {
                        sendError(session, id, "No component mounted for id '" + id + "'");
                        return;
                    }
                    invokeAction(instance, msg.path("action").asText(), msg.path("args"));
                    renderUpdate(session, id, instance);
                }
                case "unmount" -> {
                    instances.remove(id);
                    trees(session).remove(id);
                }
                default -> sendError(session, id, "Unknown message type: '" + type + "'");
            }
        } catch (Exception ex) {
            log.warn("Live message failed (type={}, id={}): {}", type, id, ex.getMessage());
            sendError(session, id, ex.getMessage());
        }
    }

    /** First render after mount: send the full tree (or state for a plain component). */
    private void renderInitial(WebSocketSession session, String id, Object instance) throws IOException {
        if (instance instanceof ServerComponent component) {
            UiNode tree = component.render();
            trees(session).put(id, tree);
            send(session, Map.of("t", "tree", "id", id, "tree", tree.toJson()));
        } else {
            sendState(session, id, instance);
        }
    }

    /** After an action: stream a minimal patch against the previous tree. */
    private void renderUpdate(WebSocketSession session, String id, Object instance) throws IOException {
        if (instance instanceof ServerComponent component) {
            UiNode previous = trees(session).get(id);
            UiNode current = component.render();
            trees(session).put(id, current);
            if (previous == null) {
                send(session, Map.of("t", "tree", "id", id, "tree", current.toJson()));
            } else {
                List<Map<String, Object>> ops = UiTreeDiff.diff(previous, current);
                send(session, Map.of("t", "patch", "id", id, "ops", ops));
            }
        } else {
            sendState(session, id, instance);
        }
    }

    private void invokeAction(Object instance, String action, JsonNode args) throws Exception {
        LiveComponentDescriptor descriptor = registry.descriptor(instance);
        Method method = descriptor.action(action);
        if (method == null) {
            throw new IllegalArgumentException("Unknown action '" + action + "'");
        }
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] callArgs = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            JsonNode arg = (args != null && args.has(i)) ? args.get(i) : null;
            callArgs[i] = coerce(arg, paramTypes[i]);
        }
        method.invoke(instance, callArgs);
    }

    private Object coerce(JsonNode node, Class<?> targetType) {
        if (node == null || node.isNull()) {
            return defaultFor(targetType);
        }
        return objectMapper.convertValue(node, targetType);
    }

    private Object defaultFor(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == double.class) {
            return 0d;
        }
        if (type == float.class) {
            return 0f;
        }
        return 0; // int / short / byte / char
    }

    private void sendState(WebSocketSession session, String id, Object instance) throws IOException {
        Map<String, Object> state = registry.descriptor(instance).readState(instance);
        send(session, Map.of("t", "state", "id", id, "state", state));
    }

    private void sendError(WebSocketSession session, String id, String messageText) {
        try {
            send(session, Map.of("t", "error", "id", id == null ? "" : id,
                    "message", messageText == null ? "error" : messageText));
        } catch (IOException ignored) {
            // Connection is gone; nothing useful to do.
        }
    }

    private void send(WebSocketSession session, Object payload) throws IOException {
        String json = objectMapper.writeValueAsString(payload);
        synchronized (session) { // WebSocketSession is not safe for concurrent sends
            session.sendMessage(new TextMessage(json));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> instances(WebSocketSession session) {
        return (Map<String, Object>) session.getAttributes().get(INSTANCES);
    }

    @SuppressWarnings("unchecked")
    private Map<String, UiNode> trees(WebSocketSession session) {
        return (Map<String, UiNode>) session.getAttributes().get(TREES);
    }
}

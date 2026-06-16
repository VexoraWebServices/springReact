package io.springreact.jsc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Computes a minimal list of patch operations to turn one {@link UiNode} tree into
 * another, so the server can stream just the changes instead of the whole tree on every
 * update. Operations are addressed by a path of child indices from the root.
 *
 * <p>Op shapes (all carry {@code op} + {@code path}):
 * <ul>
 *   <li>{@code text}    – {@code {value}} : a text leaf changed</li>
 *   <li>{@code props}   – {@code {set, remove}} : props added/changed/removed</li>
 *   <li>{@code insert}  – {@code {index, node}} : append/insert a child at path</li>
 *   <li>{@code remove}  – {@code {index}} : remove a child at path</li>
 *   <li>{@code replace} – {@code {node}} : replace the node at path entirely</li>
 * </ul>
 */
public final class UiTreeDiff {

    private UiTreeDiff() {
    }

    public static List<Map<String, Object>> diff(UiNode oldNode, UiNode newNode) {
        List<Map<String, Object>> ops = new ArrayList<>();
        diffNode(oldNode, newNode, new ArrayDeque<>(), ops);
        return ops;
    }

    private static void diffNode(UiNode oldNode, UiNode newNode,
                                 Deque<Integer> path, List<Map<String, Object>> ops) {
        // Both text: emit a text op only if the value changed.
        if (oldNode instanceof Text oldText && newNode instanceof Text newText) {
            if (!Objects.equals(oldText.value(), newText.value())) {
                ops.add(op("text", path, Map.of("value", newText.value())));
            }
            return;
        }

        // Same element type: diff props, then children by index.
        if (oldNode instanceof Element oldEl && newNode instanceof Element newEl
                && oldEl.tag.equals(newEl.tag)) {
            diffProps(oldEl, newEl, path, ops);

            int common = Math.min(oldEl.children.size(), newEl.children.size());
            for (int i = 0; i < common; i++) {
                path.addLast(i);
                diffNode(oldEl.children.get(i), newEl.children.get(i), path, ops);
                path.removeLast();
            }
            // New children appended.
            for (int i = common; i < newEl.children.size(); i++) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("index", i);
                body.put("node", newEl.children.get(i).toJson());
                ops.add(op("insert", path, body));
            }
            // Surplus old children removed, high index first so indices stay valid.
            for (int i = oldEl.children.size() - 1; i >= common; i--) {
                ops.add(op("remove", path, Map.of("index", i)));
            }
            return;
        }

        // Different node kind or tag: replace wholesale.
        ops.add(op("replace", path, Map.of("node", newNode.toJson())));
    }

    private static void diffProps(Element oldEl, Element newEl,
                                  Deque<Integer> path, List<Map<String, Object>> ops) {
        Map<String, Object> set = new LinkedHashMap<>();
        List<String> remove = new ArrayList<>();
        for (Map.Entry<String, Object> e : newEl.props.entrySet()) {
            if (!Objects.equals(oldEl.props.get(e.getKey()), e.getValue())) {
                set.put(e.getKey(), e.getValue());
            }
        }
        for (String key : oldEl.props.keySet()) {
            if (!newEl.props.containsKey(key)) {
                remove.add(key);
            }
        }
        if (!set.isEmpty() || !remove.isEmpty()) {
            Map<String, Object> body = new LinkedHashMap<>();
            if (!set.isEmpty()) {
                body.put("set", set);
            }
            if (!remove.isEmpty()) {
                body.put("remove", remove);
            }
            ops.add(op("props", path, body));
        }
    }

    private static Map<String, Object> op(String type, Deque<Integer> path, Map<String, Object> body) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("op", type);
        m.put("path", new ArrayList<>(path));
        m.putAll(body);
        return m;
    }
}

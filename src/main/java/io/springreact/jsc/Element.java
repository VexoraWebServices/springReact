package io.springreact.jsc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An element node: a tag, a map of props (attributes + event bindings) and child nodes.
 * Built via the {@link Html} DSL; mutated only during construction.
 */
public final class Element implements UiNode {

    final String tag;
    final Map<String, Object> props = new LinkedHashMap<>();
    final List<UiNode> children = new ArrayList<>();

    Element(String tag) {
        this.tag = tag;
    }

    @Override
    public Object toJson() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("tag", tag);
        if (!props.isEmpty()) {
            json.put("props", props);
        }
        if (!children.isEmpty()) {
            List<Object> kids = new ArrayList<>(children.size());
            for (UiNode child : children) {
                kids.add(child.toJson());
            }
            json.put("children", kids);
        }
        return json;
    }
}

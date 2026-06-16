package io.springreact.jsc;

import java.util.Map;

/** A text leaf node. */
public record Text(String value) implements UiNode {

    @Override
    public Object toJson() {
        return Map.of("text", value);
    }
}

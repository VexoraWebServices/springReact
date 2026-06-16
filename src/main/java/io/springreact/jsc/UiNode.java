package io.springreact.jsc;

/**
 * A node in a server-rendered UI tree — the Java equivalent of a React element. Either an
 * {@link Element} (a tag with props and children) or a {@link Text} leaf. The tree is
 * serialized to JSON and reconciled by the universal React client runtime.
 */
public sealed interface UiNode permits Element, Text {

    /** Convert to a plain JSON-serializable structure (Map / List / String). */
    Object toJson();
}

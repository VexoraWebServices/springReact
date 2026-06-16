package io.springreact.jsc;

/** A prop/attribute (or event binding) applied to an {@link Element} during construction. */
@FunctionalInterface
public interface Attr {
    void apply(Element element);
}

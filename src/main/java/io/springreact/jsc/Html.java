package io.springreact.jsc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Java "JSX" DSL. Static factories build a {@link UiNode} tree that the universal
 * React client renders. Import statically:
 *
 * <pre>{@code
 * import static io.springreact.jsc.Html.*;
 *
 * div(cls("card"),
 *     h1("Hello " + name),
 *     button(onClick("increment"), "Count: " + count),
 *     ul(items.stream().map(i -> li(i.name())).toList()));
 * }</pre>
 *
 * <p>Each element factory accepts a mix of {@link Attr} (props/events), {@link UiNode}
 * (children), {@link CharSequence} (auto-wrapped as text) and {@link Iterable}/arrays of
 * those — so you can splice mapped lists straight in.
 */
public final class Html {

    private Html() {
    }

    // --- elements ---------------------------------------------------------------

    public static Element el(String tag, Object... parts) {
        Element e = new Element(tag);
        for (Object part : parts) {
            add(e, part);
        }
        return e;
    }

    public static Element div(Object... p)     { return el("div", p); }
    public static Element span(Object... p)    { return el("span", p); }
    public static Element section(Object... p) { return el("section", p); }
    public static Element header(Object... p)  { return el("header", p); }
    public static Element main(Object... p)    { return el("main", p); }
    public static Element h1(Object... p)      { return el("h1", p); }
    public static Element h2(Object... p)      { return el("h2", p); }
    public static Element h3(Object... p)      { return el("h3", p); }
    public static Element p(Object... p)       { return el("p", p); }
    public static Element strong(Object... p)  { return el("strong", p); }
    public static Element em(Object... p)      { return el("em", p); }
    public static Element code(Object... p)    { return el("code", p); }
    public static Element a(Object... p)       { return el("a", p); }
    public static Element button(Object... p)  { return el("button", p); }
    public static Element ul(Object... p)      { return el("ul", p); }
    public static Element li(Object... p)      { return el("li", p); }
    public static Element label(Object... p)   { return el("label", p); }
    public static Element input(Object... p)   { return el("input", p); }
    public static Element form(Object... p)    { return el("form", p); }
    public static Element br()                 { return el("br"); }

    public static Text text(Object value) {
        return new Text(String.valueOf(value));
    }

    /**
     * Escape hatch: render a custom client React component (registered via
     * {@code registerWidget("Name", Comp)}) inside a Java-authored screen. Logic stays on
     * the server; the widget gets the given props plus a {@code call} to invoke actions.
     *
     * <pre>{@code widget("StarRating", attr("value", rating), attr("action", "rate"))}</pre>
     */
    public static Element widget(String name, Object... parts) {
        Element e = new Element("$widget");
        e.props.put("name", name);
        for (Object part : parts) {
            add(e, part);
        }
        return e;
    }

    // --- attributes -------------------------------------------------------------

    public static Attr cls(String value)             { return e -> e.props.put("className", value); }
    public static Attr attr(String key, Object value) { return e -> e.props.put(key, value); }
    public static Attr id(String value)              { return attr("id", value); }
    public static Attr href(String value)            { return attr("href", value); }
    public static Attr type(String value)            { return attr("type", value); }
    public static Attr placeholder(String value)     { return attr("placeholder", value); }
    public static Attr value(Object value)           { return attr("value", value); }
    public static Attr checked(boolean value)        { return attr("checked", value); }
    public static Attr disabled(boolean value)       { return attr("disabled", value); }

    // --- event bindings (resolved to server actions by the client) --------------

    /** Click → call the named server action with the given fixed args. */
    public static Attr onClick(String action, Object... args) {
        return event("onClick", action, null, args);
    }

    /** Change → call the named server action with the given fixed args. */
    public static Attr onChange(String action, Object... args) {
        return event("onChange", action, null, args);
    }

    /** Change → call the action with the input's current value as the first arg. */
    public static Attr onChangeValue(String action, Object... args) {
        return event("onChange", action, "value", args);
    }

    private static Attr event(String key, String action, String eventField, Object... args) {
        return e -> {
            Map<String, Object> binding = new LinkedHashMap<>();
            binding.put("$action", action);
            if (eventField != null) {
                binding.put("event", eventField);
            }
            if (args != null && args.length > 0) {
                binding.put("args", List.of(args));
            }
            e.props.put(key, binding);
        };
    }

    // --- internals --------------------------------------------------------------

    private static void add(Element e, Object part) {
        if (part == null) {
            return;
        }
        if (part instanceof Attr a) {
            a.apply(e);
        } else if (part instanceof UiNode n) {
            e.children.add(n);
        } else if (part instanceof CharSequence s) {
            e.children.add(new Text(s.toString()));
        } else if (part instanceof Iterable<?> it) {
            for (Object x : it) {
                add(e, x);
            }
        } else if (part instanceof Object[] arr) {
            for (Object x : arr) {
                add(e, x);
            }
        } else {
            e.children.add(new Text(String.valueOf(part)));
        }
    }
}

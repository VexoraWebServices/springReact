package io.springreact.jsc

/**
 * The Kotlin "JSX" DSL. Functions build a [UiNode] tree the universal React client renders.
 * Import the members:
 *
 * ```
 * import io.springreact.jsc.Html.*
 *
 * div(cls("card"),
 *     h1("Hello $name"),
 *     button(onClick("increment"), "Count: $count"),
 *     ul(items.map { li(it.name) }))
 * ```
 *
 * Each element factory accepts a mix of [Attr] (props/events), [UiNode] children,
 * [CharSequence] (auto-wrapped as text), and [Iterable]/arrays of those.
 *
 * Declared as an `object` with `@JvmStatic` so it reads naturally from both Kotlin and Java.
 */
object Html {

    // --- elements ---------------------------------------------------------------

    @JvmStatic fun el(tag: String, vararg parts: Any?): Element {
        val e = Element(tag)
        for (part in parts) add(e, part)
        return e
    }

    @JvmStatic fun div(vararg p: Any?) = el("div", *p)
    @JvmStatic fun span(vararg p: Any?) = el("span", *p)
    @JvmStatic fun section(vararg p: Any?) = el("section", *p)
    @JvmStatic fun header(vararg p: Any?) = el("header", *p)
    @JvmStatic fun footer(vararg p: Any?) = el("footer", *p)
    @JvmStatic fun nav(vararg p: Any?) = el("nav", *p)
    @JvmStatic fun main(vararg p: Any?) = el("main", *p)
    @JvmStatic fun h1(vararg p: Any?) = el("h1", *p)
    @JvmStatic fun h2(vararg p: Any?) = el("h2", *p)
    @JvmStatic fun h3(vararg p: Any?) = el("h3", *p)
    @JvmStatic fun p(vararg p: Any?) = el("p", *p)
    @JvmStatic fun strong(vararg p: Any?) = el("strong", *p)
    @JvmStatic fun em(vararg p: Any?) = el("em", *p)
    @JvmStatic fun code(vararg p: Any?) = el("code", *p)
    @JvmStatic fun a(vararg p: Any?) = el("a", *p)
    @JvmStatic fun button(vararg p: Any?) = el("button", *p)
    @JvmStatic fun ul(vararg p: Any?) = el("ul", *p)
    @JvmStatic fun li(vararg p: Any?) = el("li", *p)
    @JvmStatic fun label(vararg p: Any?) = el("label", *p)
    @JvmStatic fun input(vararg p: Any?) = el("input", *p)
    @JvmStatic fun form(vararg p: Any?) = el("form", *p)
    @JvmStatic fun small(vararg p: Any?) = el("small", *p)
    @JvmStatic fun br() = el("br")

    @JvmStatic fun text(value: Any?) = Text(value.toString())

    /** A placeholder a layout renders the current child screen into. */
    @JvmStatic fun slot() = Element("\$slot")

    /**
     * Escape hatch: render a custom client React component (registered via
     * `registerWidget("Name", Comp)`) inside a server-authored screen.
     */
    @JvmStatic fun widget(name: String, vararg parts: Any?): Element {
        val e = Element("\$widget")
        e.props["name"] = name
        for (part in parts) add(e, part)
        return e
    }

    // --- attributes -------------------------------------------------------------

    @JvmStatic fun cls(value: String) = Attr { it.props["className"] = value }
    @JvmStatic fun attr(key: String, value: Any?) = Attr { it.props[key] = value }
    @JvmStatic fun name(value: String) = attr("name", value)
    @JvmStatic fun id(value: String) = attr("id", value)
    @JvmStatic fun href(value: String) = attr("href", value)
    @JvmStatic fun type(value: String) = attr("type", value)
    @JvmStatic fun placeholder(value: String) = attr("placeholder", value)
    @JvmStatic fun value(value: Any?) = attr("value", value)
    @JvmStatic fun checked(value: Boolean) = attr("checked", value)
    @JvmStatic fun disabled(value: Boolean) = attr("disabled", value)

    // --- event bindings (resolved to server actions by the client) --------------

    @JvmStatic fun onClick(action: String, vararg args: Any?) = event("onClick", action, null, false, args)
    @JvmStatic fun onChange(action: String, vararg args: Any?) = event("onChange", action, null, false, args)
    @JvmStatic fun onChangeValue(action: String, vararg args: Any?) = event("onChange", action, "value", false, args)

    /** Submit: the client gathers the form's named fields into an object passed as arg 0. */
    @JvmStatic fun onSubmit(action: String, vararg args: Any?) = event("onSubmit", action, null, true, args)

    private fun event(key: String, action: String, eventField: String?, form: Boolean, args: Array<out Any?>): Attr =
        Attr { e ->
            val binding = LinkedHashMap<String, Any?>()
            binding["\$action"] = action
            if (eventField != null) binding["event"] = eventField
            if (form) binding["form"] = true
            if (args.isNotEmpty()) binding["args"] = args.toList()
            e.props[key] = binding
        }

    // --- internals --------------------------------------------------------------

    private fun add(e: Element, part: Any?) {
        when (part) {
            null -> {}
            is Attr -> part.apply(e)
            is UiNode -> e.children.add(part)
            is CharSequence -> e.children.add(Text(part.toString()))
            is Iterable<*> -> part.forEach { add(e, it) }
            is Array<*> -> part.forEach { add(e, it) }
            else -> e.children.add(Text(part.toString()))
        }
    }
}

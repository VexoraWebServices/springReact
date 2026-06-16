package com.vexora.springreact.jsc

import org.springframework.web.util.HtmlUtils

/**
 * Serializes a [UiNode] tree to an HTML string for server-side rendering (SSR). Because we
 * own the VDOM format, we can produce the initial markup directly on the JVM — no Node /
 * React-on-the-server needed. The client then takes over the same tree for interactivity.
 *
 * - Event props (objects carrying `$action`) and React-only props (`key`) are skipped.
 * - `$slot` is replaced by the provided child HTML (for layout composition).
 * - `$widget` becomes an empty placeholder the client fills with the real React component.
 */
object UiHtml {

    private val VOID = setOf(
        "area", "base", "br", "col", "embed", "hr", "img", "input",
        "link", "meta", "param", "source", "track", "wbr",
    )
    private val BOOLEAN_ATTRS = setOf("checked", "disabled", "selected", "readonly", "required")

    /** Render [node] to HTML. [slotHtml] is substituted wherever a `$slot` node appears. */
    fun render(node: UiNode, slotHtml: String = ""): String {
        val sb = StringBuilder()
        append(sb, node, slotHtml)
        return sb.toString()
    }

    private fun append(sb: StringBuilder, node: UiNode, slotHtml: String) {
        when (node) {
            is Text -> sb.append(HtmlUtils.htmlEscape(node.value))
            is Element -> appendElement(sb, node, slotHtml)
        }
    }

    private fun appendElement(sb: StringBuilder, el: Element, slotHtml: String) {
        when (el.tag) {
            "\$slot" -> {
                sb.append(slotHtml)
                return
            }
            "\$widget" -> {
                // Placeholder; the client mounts the real widget here after load.
                val name = el.props["name"]?.toString().orEmpty()
                sb.append("<span data-springreact-widget=\"")
                    .append(HtmlUtils.htmlEscape(name))
                    .append("\"></span>")
                return
            }
        }

        sb.append('<').append(el.tag)
        for ((name, value) in el.props) {
            appendAttr(sb, name, value)
        }
        if (el.tag in VOID) {
            sb.append(" />")
            return
        }
        sb.append('>')
        for (child in el.children) {
            append(sb, child, slotHtml)
        }
        sb.append("</").append(el.tag).append('>')
    }

    private fun appendAttr(sb: StringBuilder, name: String, value: Any?) {
        if (value == null) return
        // React-only or event props aren't real HTML attributes.
        if (name == "key") return
        if (value is Map<*, *>) return // event binding like { $action: ... }

        val htmlName = if (name == "className") "class" else name

        if (name in BOOLEAN_ATTRS) {
            if (value == true) sb.append(' ').append(htmlName)
            return
        }
        sb.append(' ').append(htmlName)
            .append("=\"").append(HtmlUtils.htmlEscape(value.toString())).append('"')
    }
}

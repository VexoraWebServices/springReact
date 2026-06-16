package com.vexora.springreact.jsc

/**
 * A node in a server-rendered UI tree — the Kotlin equivalent of a React element. Either
 * an [Element] (a tag with props and children) or a [Text] leaf. The tree is serialized to
 * JSON and reconciled by the universal React client runtime.
 */
sealed interface UiNode {
    /** Convert to a plain JSON-serializable structure (Map / List / String). */
    fun toJson(): Any
}

/** A text leaf node. */
data class Text(val value: String) : UiNode {
    override fun toJson(): Any = mapOf("text" to value)
}

/** An element node: a tag, props (attributes + event bindings) and child nodes. */
class Element(val tag: String) : UiNode {
    val props: MutableMap<String, Any?> = LinkedHashMap()
    val children: MutableList<UiNode> = ArrayList()

    override fun toJson(): Any {
        val json = LinkedHashMap<String, Any?>()
        json["tag"] = tag
        if (props.isNotEmpty()) json["props"] = props
        if (children.isNotEmpty()) json["children"] = children.map { it.toJson() }
        return json
    }
}

/** A prop/attribute (or event binding) applied to an [Element] during construction. */
fun interface Attr {
    fun apply(element: Element)
}

/** A server component: [render] returns a UI tree built with the [Html] DSL. */
interface ServerComponent {
    fun render(): UiNode
}
